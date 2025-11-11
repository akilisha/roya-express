import { Link } from 'wouter';
import { PageHeader, SectionHeader, SubsectionHeader } from '../../components/PageHeader';
import { Seo } from '../../components/Seo';

export function DatabaseInternals() {
  return (
    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-12 space-y-10">
      <Seo
        title="Database Internals"
        description="Understand how Roya's database plugin, migrations, JOOQ integration, and secrets management work under the hood."
      />
      <Link href="/docs/guide" class="text-roya-primary dark:text-roya-primary hover:underline inline-block mb-4">
        ← Guide
      </Link>
      <PageHeader
        title="Database Internals"
        subtitle="Deep dive into the Roya Database plugin, schema management, and runtime configuration."
        variant="gradient"
        subtitleMuted
      />

      <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark p-8 space-y-6">
        <SectionHeader title="High-Level Architecture" variant="primary" />
        <p class="text-roya-text dark:text-roya-textDark leading-relaxed">
          The database plugin is built around Helidon Config, Flyway migrations, HikariCP pooling, and JOOQ code
          generation. It exposes a thin <code class="font-mono text-sm">Database</code> facade that your application can
          retrieve via <code class="font-mono text-sm">req.get(Database.class)</code>. The facade keeps the operational
          concerns (migrations, connection lifecycle, model generation) isolated from request handlers.
        </p>
        <ul class="list-disc list-inside text-roya-text dark:text-roya-textDark space-y-2">
          <li><strong>Config precedence:</strong> system properties → environment variables → application.* → overrides.</li>
          <li><strong>Pooling:</strong> HikariCP is tuned for virtual threads (short acquisition timeout, high max pool).</li>
          <li><strong>Migrations:</strong> Flyway runs on startup via <code class="font-mono text-sm">database.migrate()</code>.</li>
          <li><strong>DSL:</strong> JOOQ is used directly; no custom ORM abstraction is wrapped around it.</li>
        </ul>
      </section>

      <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark p-8 space-y-6">
        <SectionHeader title="Configuration Precedence" />
        <p class="text-roya-text dark:text-roya-textDark leading-relaxed">
          Roya initializes Helidon Config at application startup via <code class="font-mono text-sm">RoyaConfig</code>.
          The resulting <code class="font-mono text-sm">Config</code> is registered with <code class="font-mono text-sm">services()</code>,
          so the database plugin (and your handlers) receive a consistent view: environment variables → system properties →
          classpath files.
        </p>
        <pre class="bg-black text-roya-primary text-sm p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code>{`Config config = app.services().get(Config.class);

var database = app.services().get(Database.class);
String url = config.get("database.url")
        .asString()
        .orElse("jdbc:postgresql://localhost:5432/docuRoya");`}</code></pre>
        <p class="text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          Tests can override precedence by creating their own <code class="font-mono text-sm">Config</code> instance and
          registering it with <code class="font-mono text-sm">services().singleton(Config.class, () -> customConfig)</code>
          before installing the plugin.
        </p>
      </section>

      <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark p-8 space-y-6">
        <SectionHeader title="Secrets Resolution" />
        <p class="text-roya-text dark:text-roya-textDark leading-relaxed">
          <code class="font-mono text-sm">SecretsMiddleware</code> now mirrors the same precedence rules. It detects
          Vault configuration automatically, falling back to config-based secrets for development.
        </p>
        <SubsectionHeader title="Development (Config-Backed)" />
        <pre class="bg-black text-roya-primary text-sm p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code>{`# application.yaml
secrets:
  database:
    url: jdbc:postgresql://localhost:5432/roya
    user: roya
    password: roya`}</code></pre>
        <p class="text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          Call <code class="font-mono text-sm">secrets.getOptional("database", "password")</code> to read the value.
        </p>
        <SubsectionHeader title="Production (Vault-Backed)" />
        <pre class="bg-black text-roya-primary text-sm p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code>{`# application.yaml
vault:
  url: http://vault.internal:8200
  token: s.***redacted***
  kvMount: infrastructure`}</code></pre>
        <p class="text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          The middleware registers a Vault-backed <code class="font-mono text-sm">Secrets</code> implementation that reads
          from <code class="font-mono text-sm">/v1/{mount}/data/{path}</code> using the KV v2 API.
        </p>
      </section>

      <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark p-8 space-y-6">
        <SectionHeader title="Migration Workflow" />
        <p class="text-roya-text dark:text-roya-textDark leading-relaxed">
          Flyway migrations live alongside each plugin. The Support Desk example calls <code class="font-mono text-sm">database.migrate()</code> on startup. For production you should run migrations as a dedicated job:
        </p>
        <ol class="list-decimal list-inside space-y-3 text-roya-text dark:text-roya-textDark">
          <li>Build the fat JAR with <code class="font-mono text-sm">./gradlew :roya-examples-supportdesk:shadowJar</code>.</li>
          <li>Run migrations: <code class="font-mono text-sm">java -jar ... --task migrate</code> (expose CLI entrypoint).</li>
          <li>Deploy application once migrations succeed.</li>
        </ol>
        <p class="text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          For CI/CD you can wire the migration step into GitHub Actions or DigitalOcean App Platform predeploy hooks.
        </p>
      </section>

      <section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark border border-roya-border dark:border-roya-borderDark p-8 space-y-6">
        <SectionHeader title="Query & Transaction Patterns" />
        <p class="text-roya-text dark:text-roya-textDark leading-relaxed">
          Use JOOQ's DSL for type-safe SQL. Wrap writes in <code class="font-mono text-sm">database.transaction()</code>.
          For read-heavy operations, re-use the DSL context from the <code class="font-mono text-sm">Database</code> facade.
        </p>
        <pre class="bg-black text-roya-primary text-sm p-4 rounded-lg overflow-x-auto border border-roya-borderDark"><code>{`Ticket createTicket(Database db, UUID customerId, String subject, String body) {
    return db.transaction(ctx -> {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        ctx.insertInto(SUPPORT_TICKETS)
           .set(SUPPORT_TICKETS.ID, id)
           .set(SUPPORT_TICKETS.CUSTOMER_ID, customerId)
           .set(SUPPORT_TICKETS.SUBJECT, subject)
           .set(SUPPORT_TICKETS.BODY, body)
           .set(SUPPORT_TICKETS.STATUS, "open")
           .set(SUPPORT_TICKETS.PRIORITY, "medium")
           .set(SUPPORT_TICKETS.CREATED_AT, now)
           .set(SUPPORT_TICKETS.UPDATED_AT, now)
           .execute();

        return new Ticket(id, customerId, subject, body, now);
    });
}`}</code></pre>
      </section>
    </div>
  );
}

