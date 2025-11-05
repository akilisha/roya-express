package com.akilisha.oss.roya.plugins.ai;

import com.akilisha.oss.roya.api.plugin.Application;
import com.akilisha.oss.roya.api.plugin.RoyaPlugin;
import com.akilisha.oss.roya.api.plugin.Services;
import com.akilisha.oss.roya.plugins.ai.googleadk.GoogleADKAdapter;
import com.akilisha.oss.roya.plugins.ai.langchain.LangChainAdapter;
import com.akilisha.oss.roya.plugins.ai.langgraph.LangGraphAdapter;
import com.akilisha.oss.roya.plugins.ai.library.AILibraryConfig;
import com.akilisha.oss.roya.plugins.ai.library.AILibraryFactory;
import com.akilisha.oss.roya.plugins.ai.webhooks.WebhookManagementAPI;
import com.akilisha.oss.roya.plugins.ai.webhooks.WebhookPersistenceService;
import com.akilisha.oss.roya.plugins.ai.webhooks.WebhookRegistry;
import com.akilisha.oss.roya.plugins.ai.scheduling.CronJobRegistry;
import com.akilisha.oss.roya.plugins.ai.watching.FileWatchRegistry;
import com.akilisha.oss.roya.plugins.ai.polling.PollingRegistry;
import com.akilisha.oss.roya.plugins.database.Database;

/**
 * AI plugin - unified AI/LLM integration orchestrating multiple libraries.
 *
 * Unified Architecture: All three libraries (LangChain, LangGraph, Google ADK) work together
 * through a single unified AI interface. Each library contributes its strengths:
 * - LangChain4j: LLM primitives (chat, embeddings), tool integration
 * - LangGraph4j: Stateful agent workflows, multi-node graphs, context management
 * - Google ADK: High-level agent orchestration, multi-agent systems
 *
 * Operations automatically delegate to the best library, while developers can also
 * access libraries directly for advanced use cases.
 */
public class AIPlugin implements RoyaPlugin {

    private Services services; // Store for use in setup()
    private WebhookPersistenceService webhookPersistenceService; // Store for API access

    @Override
    public String id() {
        return "ai";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Library-first AI integration supporting LangChain, Google ADK, and LangGraph";
    }

    @Override
    public void register(Services services) {
        this.services = services; // Store for later use
        services.singleton(AI.class, () -> {
            boolean enableCache = Boolean.parseBoolean(
                System.getProperty("ai.cache.enabled", "true"));

            // Build library configuration (shared across all libraries)
            var configBuilder = AILibraryConfig.builder().caching(enableCache);

            // Add OpenAI provider if configured
            String openaiKey = getConfigValue("ai.openai.apiKey", "AI_OPENAI_API_KEY", "OPENAI_API_KEY");
            if (openaiKey != null && !openaiKey.isBlank()) {
                configBuilder.provider("openai", AILibraryConfig.ProviderConfig.simple(openaiKey));
            }

            // Add Anthropic provider if configured
            String anthropicKey = getConfigValue("ai.anthropic.apiKey", "AI_ANTHROPIC_API_KEY", "ANTHROPIC_API_KEY");
            if (anthropicKey != null && !anthropicKey.isBlank()) {
                configBuilder.provider("anthropic", AILibraryConfig.ProviderConfig.simple(anthropicKey));
            }

            // Add Google Gemini provider if configured
            String geminiApiKey = getConfigValue("ai.gemini.apiKey", "AI_GEMINI_API_KEY", "GOOGLE_API_KEY", "GEMINI_API_KEY");
            String geminiProject = getConfigValue("ai.gemini.project", "AI_GEMINI_PROJECT", "GOOGLE_CLOUD_PROJECT", "GCP_PROJECT_ID");
            String geminiLocation = getConfigValue("ai.gemini.location", "AI_GEMINI_LOCATION", "GOOGLE_CLOUD_LOCATION", "GCP_REGION");
            
            if (geminiApiKey != null && !geminiApiKey.isBlank()) {
                // Gemini requires project, location, and API key
                var geminiConfig = geminiProject != null && !geminiProject.isBlank() && 
                                  geminiLocation != null && !geminiLocation.isBlank()
                    ? AILibraryConfig.ProviderConfig.gemini(geminiApiKey, geminiProject, geminiLocation)
                    : AILibraryConfig.ProviderConfig.simple(geminiApiKey); // Fallback if project/location not set
                configBuilder.provider("gemini", geminiConfig);
            }

            var config = configBuilder.build();

            // Create all three library adapters (they work together!)
            // LangChain4j: Best for LLM primitives (chat, embeddings, tools)
            LangChainAdapter langChain = createLangChain(config);

            // LangGraph4j: Best for stateful agent workflows (StateGraph, multi-node)
            LangGraphAdapter langGraph = createLangGraph(config);

            // Google ADK: Best for high-level agent orchestration (when available)
            GoogleADKAdapter googleADK = createGoogleADK(config);

            // Combine all three into a unified service
            // Each library contributes its strengths to the unified AI interface
            if (googleADK != null) {
                return new UnifiedAIService(langChain, langGraph, googleADK);
            } else {
                return new UnifiedAIService(langChain, langGraph);
            }
        });
    }

    private LangChainAdapter createLangChain(AILibraryConfig config) {
        var langChainLibrary = AILibraryFactory.create("langchain");
        AI ai = langChainLibrary.create(config);
        if (ai instanceof LangChainAdapter) {
            return (LangChainAdapter) ai;
        }
        throw new IllegalStateException("LangChain library did not return LangChainAdapter");
    }

    private LangGraphAdapter createLangGraph(AILibraryConfig config) {
        var langGraphLibrary = AILibraryFactory.create("langgraph");
        AI ai = langGraphLibrary.create(config);
        if (ai instanceof LangGraphAdapter) {
            return (LangGraphAdapter) ai;
        }
        throw new IllegalStateException("LangGraph library did not return LangGraphAdapter");
    }

    private GoogleADKAdapter createGoogleADK(AILibraryConfig config) {
        try {
            var adkLibrary = AILibraryFactory.create("googleadk");
            var ai = adkLibrary.create(config);
            if (ai instanceof GoogleADKAdapter) {
                return (GoogleADKAdapter) ai;
            }
            return null; // ADK not yet implemented, return null (optional)
        } catch (UnsupportedOperationException e) {
            // Google ADK not yet implemented, that's OK
            return null;
        } catch (Exception e) {
            // Other errors, return null (ADK is optional)
            return null;
        }
    }


    private String getConfigValue(String systemProp, String... envVars) {
        String value = System.getProperty(systemProp);
        if (value != null && !value.isBlank()) {
            return value;
        }
        for (String envVar : envVars) {
            value = System.getenv(envVar);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    @Override
    public void setup(Application app) {
        // Initialize CronJobRegistry (Quartz scheduler)
        try {
            CronJobRegistry.getInstance().initialize();
        } catch (Exception e) {
            System.out.println("⚠️  CronJobRegistry initialization failed: " + e.getMessage());
            System.out.println("   Scheduled workflows will not be available");
        }
        
        // Initialize FileWatchRegistry (WatchService)
        try {
            FileWatchRegistry.getInstance().initialize();
        } catch (Exception e) {
            System.out.println("⚠️  FileWatchRegistry initialization failed: " + e.getMessage());
            System.out.println("   File watch workflows will not be available");
        }
        
        // Initialize PollingRegistry (ScheduledExecutorService)
        try {
            PollingRegistry.getInstance().initialize();
        } catch (Exception e) {
            System.out.println("⚠️  PollingRegistry initialization failed: " + e.getMessage());
            System.out.println("   Polling workflows will not be available");
        }
        
        // Register webhook routes with the Roya application
        WebhookRegistry.getInstance().registerRoutes(app);
        
        // Initialize webhook persistence (if Database plugin is available)
        if (services.has(Database.class)) {
            try {
                Database db = services.get(Database.class);
                webhookPersistenceService = new WebhookPersistenceService(
                    new WebhookPersistenceService.DatabaseWebhookStore(db)
                );
                webhookPersistenceService.initialize(app);
                webhookPersistenceService.loadAndRegisterAll();
                System.out.println("✓ Webhook persistence enabled (Database)");
                
                // Register webhook management API endpoints
                WebhookManagementAPI managementAPI = new WebhookManagementAPI(webhookPersistenceService);
                managementAPI.registerRoutes(app);
                System.out.println("✓ Webhook Management API registered at /api/webhooks");
            } catch (Exception e) {
                // Error initializing persistence - use in-memory
                System.out.println("⚠️  Webhook persistence initialization failed: " + e.getMessage());
                System.out.println("   Using in-memory webhook storage (no persistence)");
                
                // Still register API with in-memory store
                webhookPersistenceService = new WebhookPersistenceService(
                    new WebhookPersistenceService.InMemoryWebhookStore()
                );
                webhookPersistenceService.initialize(app);
                WebhookManagementAPI managementAPI = new WebhookManagementAPI(webhookPersistenceService);
                managementAPI.registerRoutes(app);
            }
        } else {
            // Fallback to in-memory (no persistence across restarts)
            System.out.println("⚠️  Database plugin not found - webhooks will not persist across restarts");
            webhookPersistenceService = new WebhookPersistenceService(
                new WebhookPersistenceService.InMemoryWebhookStore()
            );
            webhookPersistenceService.initialize(app);
            WebhookManagementAPI managementAPI = new WebhookManagementAPI(webhookPersistenceService);
            managementAPI.registerRoutes(app);
            System.out.println("✓ Webhook Management API registered at /api/webhooks (in-memory)");
        }
    }

    @Override
    public void start() throws Exception {
        String openaiKey = getConfigValue("ai.openai.apiKey", "AI_OPENAI_API_KEY", "OPENAI_API_KEY");
        String anthropicKey = getConfigValue("ai.anthropic.apiKey", "AI_ANTHROPIC_API_KEY", "ANTHROPIC_API_KEY");
        String geminiKey = getConfigValue("ai.gemini.apiKey", "AI_GEMINI_API_KEY", "GOOGLE_API_KEY", "GEMINI_API_KEY");
        boolean caching = Boolean.parseBoolean(System.getProperty("ai.cache.enabled", "true"));

        System.out.println("✓ AIPlugin: Unified AI service initialized");
        System.out.println("  - Architecture: All libraries working together");
        System.out.println("    • LangChain4j: LLM primitives, embeddings, tools");
        System.out.println("    • LangGraph4j: Stateful workflows, multi-node agents");
        System.out.println("    • Google ADK: High-level orchestration (optional)");
        System.out.println("  - Providers: " + buildProviderList(openaiKey, anthropicKey, geminiKey));
        System.out.println("  - Caching: " + (caching ? "enabled" : "disabled"));
        System.out.println("  - Usage: AI ai = req.get(AI.class);");
        System.out.println("  - Direct access: ((UnifiedAIService) ai).langChain() / .langGraph() / .googleADK()");
    }

    private String buildProviderList(String openaiKey, String anthropicKey, String geminiKey) {
        var providers = new java.util.ArrayList<String>();
        if (openaiKey != null) providers.add("openai");
        if (anthropicKey != null) providers.add("anthropic");
        if (geminiKey != null) providers.add("gemini");
        return providers.isEmpty() ? "none" : String.join(", ", providers);
    }

    @Override
    public void stop() throws Exception {
        // Shutdown PollingRegistry
        try {
            PollingRegistry.getInstance().shutdown();
        } catch (Exception e) {
            System.err.println("Error shutting down PollingRegistry: " + e.getMessage());
        }
        
        // Shutdown FileWatchRegistry
        try {
            FileWatchRegistry.getInstance().shutdown();
        } catch (Exception e) {
            System.err.println("Error shutting down FileWatchRegistry: " + e.getMessage());
        }
        
        // Shutdown CronJobRegistry
        try {
            CronJobRegistry.getInstance().shutdown();
        } catch (Exception e) {
            System.err.println("Error shutting down CronJobRegistry: " + e.getMessage());
        }
    }
}



