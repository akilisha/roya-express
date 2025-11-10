package com.akilisha.oss.roya.examples.supportdesk;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AI.ChunkingOptions;
import com.akilisha.oss.roya.plugins.ai.RAGOptions;
import com.akilisha.oss.roya.plugins.ai.RAGResponse;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.core.middleware.Compression;
import com.akilisha.oss.roya.core.middleware.Cors;
import com.akilisha.oss.roya.core.middleware.Helmet;
import com.akilisha.oss.roya.core.middleware.Morgan;
import com.akilisha.oss.roya.core.middleware.RateLimit;
import com.akilisha.oss.roya.core.middleware.Session;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.cache.Cache;
import com.akilisha.oss.roya.plugins.cache.CachePlugin;
import com.akilisha.oss.roya.plugins.database.Database;
import com.akilisha.oss.roya.plugins.database.DatabasePlugin;
import com.akilisha.oss.roya.plugins.email.Email;
import com.akilisha.oss.roya.plugins.email.EmailMessage;
import com.akilisha.oss.roya.plugins.email.EmailPlugin;
import com.akilisha.oss.roya.plugins.email.EmailResult;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.jooq.Record;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Reference support-desk application showing Roya + plugins working together.
 *
 * <p>Demos:</p>
 * <ul>
 *   <li>HTTP routing & middleware (CORS, Helmet, compression, rate limiting, sessions)</li>
 *   <li>Database plugin (PostgreSQL) for ticket storage</li>
 *   <li>Cache plugin for computed ticket summaries</li>
 *   <li>Email plugin for notifications (falls back to console output)</li>
 *   <li>AI plugin (LangChain4j AI Services + RAG) for agent drafted replies</li>
 * </ul>
 */
public class SupportDeskApplication {

    private static final String KB_COLLECTION = "supportdesk-kb";
    private static final String DEFAULT_FROM_EMAIL = "supportdesk@example.com";

    public static void main(String[] args) {
        var app = Roya.create();

        configureMiddleware(app);

        Database database;
        Cache cache;
        Email email;
        AI ai = null;

        try {
            database = registerDatabase(app);
        } catch (Exception e) {
            throw new IllegalStateException("Database plugin failed to start. Configure DATABASE_URL.", e);
        }

        cache = registerCache(app);
        email = registerEmail(app);

        try {
            ai = registerAI(app);
        } catch (Exception e) {
            System.err.println("⚠️  AI plugin failed to start: " + e.getMessage());
            System.err.println("    Configure AI credentials (e.g. OPENAI_API_KEY) to enable AI drafted replies.");
        }

        initialiseSchema(database);

        SupportAgentService supportAgent = ai != null ? new SupportAgentService(ai) : null;
        TicketRepository tickets = new TicketRepository(database);
        TicketCache ticketCache = cache != null ? new TicketCache(cache) : TicketCache.noop();
        NotificationService notifications = new NotificationService(email);

        if (supportAgent != null) {
            seedKnowledgeBase(ai);
        }

        registerRoutes(app, tickets, ticketCache, supportAgent, notifications);

        int port = Integer.parseInt(System.getProperty("PORT", "8080"));
        app.listen(port);
        System.out.println("🚀 Support Desk API running on http://localhost:" + port);
    }

    private static void configureMiddleware(Roya app) {
        app.use(Morgan.combined());
        app.use(Cors.cors());
        app.use(Helmet.helmet());
        app.use(Compression.compression());
        app.use(BodyParser.bodyParser());
        app.use(RateLimit.rateLimit());
        app.use(Session.session());
    }

    private static Database registerDatabase(Roya app) throws Exception {
        var plugin = new DatabasePlugin();
        plugin.register(app.services());
        plugin.start();
        Database database = app.services().get(Database.class);
//        database.migrate();
        return database;
    }

    private static Cache registerCache(Roya app) {
        var plugin = new CachePlugin();
        plugin.register(app.services());
        return app.services().has(Cache.class) ? app.services().get(Cache.class) : null;
    }

    private static Email registerEmail(Roya app) {
        var plugin = new EmailPlugin();
        plugin.register(app.services());
        return app.services().has(Email.class) ? app.services().get(Email.class) : null;
    }

    private static AI registerAI(Roya app) throws Exception {
        var plugin = new AIPlugin();
        plugin.register(app.services());
        plugin.start();
        return app.services().get(AI.class);
    }

    private static void initialiseSchema(Database database) {
        database.transaction(ctx -> {
            ctx.execute("""
                create table if not exists support_customers (
                    id uuid primary key,
                    email text not null unique,
                    name text not null,
                    company text,
                    created_at timestamptz default now()
                );
            """);

            ctx.execute("""
                create table if not exists support_tickets (
                    id uuid primary key,
                    customer_id uuid not null references support_customers(id) on delete cascade,
                    subject text not null,
                    body text not null,
                    status text not null,
                    priority text not null,
                    assigned_to text,
                    created_at timestamptz default now(),
                    updated_at timestamptz default now()
                );
            """);

            ctx.execute("""
                create table if not exists support_ticket_messages (
                    id uuid primary key,
                    ticket_id uuid not null references support_tickets(id) on delete cascade,
                    author text not null,
                    message text not null,
                    created_at timestamptz default now()
                );
            """);
            return null;
        });
    }

    private static void seedKnowledgeBase(AI ai) {
        try {
            var resource = SupportDeskApplication.class.getResource("/supportdesk-kb");
            if (resource == null) {
                System.out.println("ℹ️  No knowledge base directory found (supportdesk-kb). Skipping indexing.");
                return;
            }

            Path kbPath = Paths.get(resource.toURI());
            if (!Files.isDirectory(kbPath)) {
                System.out.println("ℹ️  Knowledge base path is not a directory. Skipping indexing.");
                return;
            }

            ai.vectors().createCollection(KB_COLLECTION);
            ai.vectors().indexPath(KB_COLLECTION, kbPath, ChunkingOptions.small());
            System.out.println("📚 Indexed knowledge base into collection " + KB_COLLECTION);
        } catch (URISyntaxException | RuntimeException e) {
            System.err.println("⚠️  Failed to index knowledge base: " + e.getMessage());
        }
    }

    private static void registerRoutes(Roya app,
                                       TicketRepository tickets,
                                       TicketCache ticketCache,
                                       SupportAgentService supportAgent,
                                       NotificationService notifications) {
        app.get("/health", (req, res, next) ->
            res.json(Map.of("status", "ok", "service", "support-desk", "timestamp", Instant.now()))
        );

        // --- Customers ----------------------------------------------------
        app.post("/api/customers", (req, res, next) -> {
            try {
                Map<String, Object> body = req.body(Map.class);
                String email = (String) body.get("email");
                String name = (String) body.get("name");
                String company = (String) body.getOrDefault("company", "");

                if (email == null || name == null) {
                    res.status(400).json(Map.of("error", "email and name are required"));
                    return;
                }

                Customer customer = tickets.createCustomer(email, name, company);
                res.status(201).json(customer.toResponse());
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        app.get("/api/customers", (req, res, next) -> {
            try {
                res.json(Map.of("customers", tickets.listCustomers().stream()
                    .map(Customer::toResponse)
                    .toList()));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // --- Tickets ------------------------------------------------------
        app.post("/api/tickets", (req, res, next) -> {
            try {
                Map<String, Object> body = req.body(Map.class);
                UUID customerId = UUID.fromString(body.get("customerId").toString());
                String subject = (String) body.get("subject");
                String ticketBody = (String) body.get("body");
                String priority = (String) body.getOrDefault("priority", "medium");

                Ticket ticket = tickets.createTicket(customerId, subject, ticketBody, priority);
                ticketCache.evictTicket(ticket.id());
                res.status(201).json(ticket.toResponse());
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        app.get("/api/tickets", (req, res, next) -> {
            try {
                String status = req.query().get("status").orElse(null);
                String priority = req.query().get("priority").orElse(null);
                res.json(Map.of(
                    "tickets", tickets.listTickets(status, priority).stream()
                        .map(ticketCache::withCachedSummary)
                        .toList()
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        app.get("/api/tickets/:id", (req, res, next) -> {
            try {
                UUID id = UUID.fromString(req.params().get("id").orElseThrow());
                TicketDetails details = tickets.getTicket(id)
                    .orElseThrow(() -> new NoSuchElementException("Ticket not found"));
                res.json(details.toResponse(ticketCache.lookupSummary(id)));
            } catch (NoSuchElementException e) {
                res.status(404).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        app.post("/api/tickets/:id/assign", (req, res, next) -> {
            try {
                UUID id = UUID.fromString(req.params().get("id").orElseThrow());
                Map<String, Object> body = req.body(Map.class);
                String assignee = (String) body.getOrDefault("assignee", "unassigned");
                Ticket ticket = tickets.assignTicket(id, assignee);
                ticketCache.evictTicket(id);
                res.json(ticket.toResponse());
            } catch (NoSuchElementException e) {
                res.status(404).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        app.post("/api/tickets/:id/status", (req, res, next) -> {
            try {
                UUID id = UUID.fromString(req.params().get("id").orElseThrow());
                Map<String, Object> body = req.body(Map.class);
                String status = (String) body.getOrDefault("status", "open");
                Ticket ticket = tickets.updateStatus(id, status);
                ticketCache.evictTicket(id);
                res.json(ticket.toResponse());
            } catch (NoSuchElementException e) {
                res.status(404).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        app.post("/api/tickets/:id/reply", (req, res, next) -> {
            if (supportAgent == null) {
                res.status(503).json(Map.of("error", "AI drafted replies are disabled - configure AI credentials."));
                return;
            }

            try {
                UUID id = UUID.fromString(req.params().get("id").orElseThrow());
                Map<String, Object> body = req.body(Map.class);
                String prompt = (String) body.getOrDefault("prompt", "Draft a helpful reply to the customer.");

                TicketDetails ticket = tickets.getTicket(id)
                    .orElseThrow(() -> new NoSuchElementException("Ticket not found"));

            TicketReply reply = supportAgent.draftReply(ticket, prompt);
                tickets.recordMessage(ticket.ticket().id(), "agent", reply.reply());
                ticketCache.cacheSummary(ticket.ticket().id(), reply.summary());
                notifications.notify(ticket.customer().email(), "Reply drafted for ticket " + ticket.ticket().subject(), reply.reply());

                res.json(Map.of(
                    "ticket", ticket.ticket().toResponse(),
                    "reply", reply.reply(),
                    "summary", reply.summary(),
                    "citations", reply.citations()
                ));
            } catch (NoSuchElementException e) {
                res.status(404).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });
    }

    // -----------------------------------------------------------------
    // Domain + infrastructure helpers
    // -----------------------------------------------------------------

    enum TicketStatus { open, in_progress, escalated, resolved, closed }
    enum TicketPriority { low, medium, high, urgent }

    record Customer(UUID id, String email, String name, String company, Instant createdAt) {
        Map<String, Object> toResponse() {
            return Map.of(
                "id", id,
                "email", email,
                "name", name,
                "company", company,
                "createdAt", createdAt
            );
        }
    }

    record Ticket(UUID id, UUID customerId, String subject, String body, TicketStatus status,
                  TicketPriority priority, String assignedTo, Instant createdAt, Instant updatedAt) {
        Map<String, Object> toResponse() {
            return Map.of(
                "id", id,
                "customerId", customerId,
                "subject", subject,
                "body", body,
                "status", status.name(),
                "priority", priority.name(),
                "assignedTo", Optional.ofNullable(assignedTo).orElse(""),
                "createdAt", createdAt,
                "updatedAt", updatedAt
            );
        }
    }

    record Message(UUID id, String author, String message, Instant createdAt) {
        Map<String, Object> toResponse() {
            return Map.of(
                "id", id,
                "author", author,
                "message", message,
                "createdAt", createdAt
            );
        }
    }

    record TicketDetails(Ticket ticket, Customer customer, List<Message> messages) {
        Map<String, Object> toResponse(String cachedSummary) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("ticket", ticket.toResponse());
            response.put("customer", customer.toResponse());
            response.put("messages", messages.stream().map(Message::toResponse).toList());
            if (cachedSummary != null) {
                response.put("cachedSummary", cachedSummary);
            }
            return response;
        }
    }

    static class TicketRepository {
        private final Database database;

        TicketRepository(Database database) {
            this.database = database;
        }

        Customer createCustomer(String email, String name, String company) {
            UUID id = UUID.randomUUID();
            Instant created = Instant.now();
            String createdTimestamp = toTimestamp(created);
            database.transaction(ctx -> {
                ctx.execute("insert into support_customers (id, email, name, company, created_at) values (?, ?, ?, ?, ?::timestamptz)",
                    id, email, name, company, createdTimestamp);
                return null;
            });
            return new Customer(id, email, name, company, created);
        }

        List<Customer> listCustomers() {
            return database.dsl().fetch("select id, email, name, company, created_at from support_customers order by created_at desc")
                .map(r -> new Customer(
                    r.get("id", UUID.class),
                    r.get("email", String.class),
                    r.get("name", String.class),
                    r.get("company", String.class),
                    toInstant(r.get("created_at"))
                ));
        }

        Ticket createTicket(UUID customerId, String subject, String body, String priority) {
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            String timestamp = toTimestamp(now);
            database.transaction(ctx -> {
                ctx.execute("insert into support_tickets (id, customer_id, subject, body, status, priority, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?::timestamptz, ?::timestamptz)",
                    id, customerId, subject, body, TicketStatus.open.name(), TicketPriority.valueOf(priority.toLowerCase()).name(), timestamp, timestamp);
                ctx.execute("insert into support_ticket_messages (id, ticket_id, author, message, created_at) values (?, ?, ?, ?, ?::timestamptz)",
                    UUID.randomUUID(), id, "customer", body, timestamp);
                return null;
            });
            return new Ticket(id, customerId, subject, body, TicketStatus.open, TicketPriority.valueOf(priority.toLowerCase()), null, now, now);
        }

        List<Ticket> listTickets(String status, String priority) {
            StringBuilder sql = new StringBuilder("select id, customer_id, subject, body, status, priority, assigned_to, created_at, updated_at from support_tickets");
            List<Object> params = new ArrayList<>();
            List<String> clauses = new ArrayList<>();
            if (status != null) {
                clauses.add("status = ?");
                params.add(status);
            }
            if (priority != null) {
                clauses.add("priority = ?");
                params.add(priority);
            }
            if (!clauses.isEmpty()) {
                sql.append(" where ").append(String.join(" and ", clauses));
            }
            sql.append(" order by created_at desc");

            return database.dsl().fetch(sql.toString(), params.toArray())
                .map(this::toTicket);
        }

        Optional<TicketDetails> getTicket(UUID id) {
            Record record = database.dsl().fetchOne("""
                select t.id as ticket_id,
                       t.customer_id as ticket_customer_id,
                       t.subject,
                       t.body,
                       t.status,
                       t.priority,
                       t.assigned_to,
                       t.created_at,
                       t.updated_at,
                       c.id as customer_id,
                       c.email,
                       c.name,
                       c.company,
                       c.created_at as customer_created
                from support_tickets t
                join support_customers c on c.id = t.customer_id
                where t.id = ?
            """, id);

            if (record == null) {
                return Optional.empty();
            }

            Ticket ticket = new Ticket(
                record.get("ticket_id", UUID.class),
                record.get("ticket_customer_id", UUID.class),
                record.get("subject", String.class),
                record.get("body", String.class),
                TicketStatus.valueOf(record.get("status", String.class)),
                TicketPriority.valueOf(record.get("priority", String.class)),
                record.get("assigned_to", String.class),
                toInstant(record.get("created_at")),
                toInstant(record.get("updated_at"))
            );

            Customer customer = new Customer(
                record.get("customer_id", UUID.class),
                record.get("email", String.class),
                record.get("name", String.class),
                record.get("company", String.class),
                toInstant(record.get("customer_created"))
            );

            List<Message> messages = database.dsl().fetch("select id, author, message, created_at from support_ticket_messages where ticket_id = ? order by created_at asc", id)
                .map(r -> new Message(
                    r.get("id", UUID.class),
                    r.get("author", String.class),
                    r.get("message", String.class),
                    toInstant(r.get("created_at"))
                ));

            return Optional.of(new TicketDetails(ticket, customer, messages));
        }

        Ticket assignTicket(UUID id, String assignee) {
            Instant now = Instant.now();
            String timestamp = toTimestamp(now);
            database.transaction(ctx -> {
                ctx.execute("update support_tickets set assigned_to = ?, updated_at = ?::timestamptz where id = ?", assignee, timestamp, id);
                ctx.execute("insert into support_ticket_messages (id, ticket_id, author, message, created_at) values (?, ?, ?, ?, ?::timestamptz)",
                    UUID.randomUUID(), id, "system", "Ticket assigned to " + assignee, timestamp);
                return null;
            });
            return getTicket(id).map(TicketDetails::ticket).orElseThrow();
        }

        Ticket updateStatus(UUID id, String status) {
            Instant now = Instant.now();
            String timestamp = toTimestamp(now);
            database.transaction(ctx -> {
                ctx.execute("update support_tickets set status = ?, updated_at = ?::timestamptz where id = ?", status, timestamp, id);
                ctx.execute("insert into support_ticket_messages (id, ticket_id, author, message, created_at) values (?, ?, ?, ?, ?::timestamptz)",
                    UUID.randomUUID(), id, "system", "Status changed to " + status, timestamp);
                return null;
            });
            return getTicket(id).map(TicketDetails::ticket).orElseThrow();
        }

        void recordMessage(UUID ticketId, String author, String message) {
            Instant now = Instant.now();
            String timestamp = toTimestamp(now);
            database.transaction(ctx -> {
                ctx.execute("insert into support_ticket_messages (id, ticket_id, author, message, created_at) values (?, ?, ?, ?, ?::timestamptz)",
                    UUID.randomUUID(), ticketId, author, message, timestamp);
                ctx.execute("update support_tickets set updated_at = ?::timestamptz where id = ?", timestamp, ticketId);
                return null;
            });
        }

        private Ticket toTicket(Record record) {
            return new Ticket(
                record.get("id", UUID.class),
                record.get("customer_id", UUID.class),
                record.get("subject", String.class),
                record.get("body", String.class),
                TicketStatus.valueOf(record.get("status", String.class)),
                TicketPriority.valueOf(record.get("priority", String.class)),
                record.get("assigned_to", String.class),
                toInstant(record.get("created_at")),
                toInstant(record.get("updated_at"))
            );
        }

        private Instant toInstant(Object value) {
            if (value instanceof Instant instant) {
                return instant;
            }
            if (value instanceof java.sql.Timestamp timestamp) {
                return timestamp.toInstant();
            }
            if (value instanceof LocalDate date) {
                return date.atStartOfDay().toInstant(ZoneOffset.UTC);
            }
            return Instant.now();
        }

        private String toTimestamp(Instant instant) {
            return instant.atOffset(ZoneOffset.UTC).toString();
        }
    }

    static class TicketCache {
        private final Cache cache;

        private TicketCache(Cache cache) {
            this.cache = cache;
        }

        static TicketCache noop() {
            return new TicketCache(null);
        }

        Map<String, Object> withCachedSummary(Ticket ticket) {
            Map<String, Object> response = new LinkedHashMap<>(ticket.toResponse());
            if (cache != null) {
                cache.get(summaryKey(ticket.id()), String.class)
                    .ifPresent(summary -> response.put("cachedSummary", summary));
            }
            return response;
        }

        void cacheSummary(UUID ticketId, String summary) {
            if (cache != null) {
                cache.set(summaryKey(ticketId), summary);
            }
        }

        String lookupSummary(UUID ticketId) {
            if (cache == null) {
                return null;
            }
            return cache.get(summaryKey(ticketId), String.class).orElse(null);
        }

        void evictTicket(UUID ticketId) {
            if (cache != null) {
                cache.delete(summaryKey(ticketId));
            }
        }

        private String summaryKey(UUID id) {
            return "supportdesk:ticket:" + id + ":summary";
        }
    }

    static class SupportAgentService {
        private final AI ai;
        private final SupportAssistant assistant;

        SupportAgentService(AI ai) {
            this.ai = ai;
            this.assistant = ai.aiService(SupportAssistant.class, builder -> builder
                .chatMemory(MessageWindowChatMemory.withMaxMessages(25))
            );
        }

        TicketReply draftReply(TicketDetails ticket, String operatorPrompt) {
            String ticketContext = "Subject: " + ticket.ticket().subject() + "\n" +
                "Body: " + ticket.ticket().body() + "\n" +
                "Customer: " + ticket.customer().name() + " (" + ticket.customer().company() + ")";

            String question = "Provide guidance for: " + ticket.ticket().subject();
            RAGResponse rag = ai.ragApi().ask(question, RAGOptions.builder()
                .collection(KB_COLLECTION)
                .topK(5)
                .rerank(true)
                .minScore(0.5)
                .build());

            String knowledge = rag != null ? rag.answer() : "No knowledge base context";
            String citations = (rag != null && rag.sources() != null) ? rag.sources().stream()
                .map(doc -> {
                    var metadata = doc.metadata() != null ? doc.metadata() : Map.<String, Object>of();
                    Object title = metadata.getOrDefault("title", doc.id());
                    Object source = metadata.getOrDefault("source", metadata.getOrDefault("url", ""));
                    String titleText = title != null ? title.toString() : "";
                    String sourceText = source != null ? source.toString() : "";
                    if (!sourceText.isBlank()) {
                        return titleText + " - " + sourceText;
                    }
                    return titleText;
                })
                .filter(line -> line != null && !line.isBlank())
                .collect(Collectors.joining("\n")) : "";

            String draft = assistant.compose(ticketContext, knowledge, operatorPrompt);
            String summary = assistant.summarise(ticketContext, draft);

            return new TicketReply(draft, summary, citations);
        }

        interface SupportAssistant {
            @SystemMessage("You are a senior customer support engineer. Draft empathetic, actionable replies.")
            @UserMessage("Ticket Context:\n{{ticket}}\n\nKnowledge:\n{{knowledge}}\n\nOperator Instructions:\n{{prompt}}\n\nRespond with a full email to the customer.")
            String compose(
                    @V("ticket") String ticket,
                    @V("knowledge") String knowledge,
                    @V("prompt") String prompt
            );

            @SystemMessage("You are a concise technical writer. Summarise tickets for hand-off notes.")
            @UserMessage("Ticket Context:\n{{ticket}}\n\nDraft Reply:\n{{reply}}\n\nSummarise in 3 bullet points.")
            String summarise(
                    @V("ticket") String ticket,
                    @V("reply") String reply
            );
        }
    }

    record TicketReply(String reply, String summary, String citations) {}

    static class NotificationService {
        private final Email emailService;

        NotificationService(Email emailService) {
            this.emailService = emailService;
        }

        void notify(String to, String subject, String body) {
            if (emailService == null) {
                System.out.println("📬 Email notification (simulated) → " + to + "\n" + subject + "\n" + body + "\n");
                return;
            }

            EmailMessage message = EmailMessage.builder()
                .from(DEFAULT_FROM_EMAIL)
                .to(to)
                .subject(subject)
                .textBody(body)
                .build();

            CompletableFuture<EmailResult> result = emailService.send(message);
            result.thenAccept(status ->
                System.out.println("📬 Email notification status → " + status.messageId())
            ).exceptionally(error -> {
                System.err.println("⚠️  Failed to send email notification: " + error.getMessage());
                return null;
            });
        }
    }
}
