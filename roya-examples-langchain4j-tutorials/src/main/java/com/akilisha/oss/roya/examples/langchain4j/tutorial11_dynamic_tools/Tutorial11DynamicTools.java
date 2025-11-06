package com.akilisha.oss.roya.examples.langchain4j.tutorial11_dynamic_tools;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.code.judge0.Judge0JavaScriptExecutionTool;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;
import java.util.Map;

/**
 * Tutorial 11: Dynamic Tools
 *
 * <p>Recreates LangChain4j's dynamic tools tutorial.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>Selecting tools dynamically based on request context</li>
 *   <li>Creating tool instances programmatically</li>
 *   <li>Filtering tools based on user permissions or context</li>
 *   <li>Adaptive AI assistants with different capabilities</li>
 *   <li>Role-based or tenant-based tool access</li>
 * </ul>
 *
 * <p>LangChain4j Approach:
 * <pre>
 * // Select tools dynamically
 * List&lt;Object&gt; tools = new ArrayList&lt;&gt;();
 * if (userCanAdd) tools.add(new AddTool());
 * if (userCanMultiply) tools.add(new MultiplyTool());
 *
 * Assistant assistant = AiServices.builder(Assistant.class)
 *     .chatModel(chatModel)
 *     .tools(tools)
 *     .build();
 * </pre>
 *
 * <p>Roya Approach:
 * <pre>
 * AI ai = req.get(AI.class);
 *
 * // Select tools dynamically based on request
 * CalculatorTools tools = selectTools(req);
 *
 * Assistant assistant = ai.aiService(Assistant.class, builder -> {
 *     builder.tools(tools); // Dynamic selection!
 * });
 * </pre>
 *
 * <p><b>Key Points:</b>
 * <ul>
 *   <li>Tools can be selected/filtered at runtime based on context</li>
 *   <li>Different users/tenants can have different tool sets</li>
 *   <li>Tools adapt based on permissions, roles, or configuration</li>
 *   <li>Enables flexible, context-aware AI assistants</li>
 *   <li>Perfect for multi-tenant or role-based access control</li>
 * </ul>
 *
 * <p>HTTP Endpoints:
 * <ul>
 *   <li>{@code POST /calculator} - Calculator with dynamically selected tools</li>
 *   <li>{@code POST /custom} - Custom tools based on request</li>
 *   <li>{@code POST /code} - Code execution with Judge0 JavaScript tool (dynamic tool)</li>
 *   <li>{@code GET /tools} - List available tools</li>
 *   <li>{@code GET /} - Root endpoint with instructions</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * // Calculator with selected tools
 * curl -X POST http://localhost:3011/calculator \
 *   -H "Content-Type: application/json" \
 *   -d '{"query": "What is 10 + 20?", "tools": ["add", "multiply"]}'
 *
 * // Custom tools
 * curl -X POST http://localhost:3011/custom \
 *   -H "Content-Type: application/json" \
 *   -d '{"query": "What is 5 * 3?", "operations": ["multiply", "divide"]}'
 *
 * // Code execution with Judge0 (dynamic tool)
 * curl -X POST http://localhost:3011/code \
 *   -H "Content-Type: application/json" \
 *   -d '{"query": "Calculate factorial of 5 using JavaScript"}'
 * </pre>
 *
 * <p><b>Judge0 Integration:</b>
 * <ul>
 *   <li>Judge0JavaScriptExecutionTool executes JavaScript code via Judge0 service</li>
 *   <li>Perfect example of dynamic tool addition - code execution can be enabled/disabled</li>
 *   <li>Demonstrates tools that provide external capabilities (code execution)</li>
 *   <li>Requires RapidAPI key - configure via environment variable or system property:
 *     <ul>
 *       <li>Environment variable: {@code RAPIDAPI_KEY} or {@code RAPID_API_KEY}</li>
 *       <li>System property: {@code -Drapidapi.key=your-key}</li>
 *       <li>Get your key from: https://rapidapi.com/judge0-official/api/judge0-ce</li>
 *     </ul>
 *   </li>
 * </ul>
 */
public class Tutorial11DynamicTools {

    /**
     * Code Execution Assistant AI Service interface.
     * This demonstrates adding Judge0 as a dynamic tool.
     */
    interface CodeExecutionAssistant {
        @SystemMessage("You are a helpful assistant that can execute JavaScript code. " +
                "When asked to perform calculations or execute code, use the available tools.")
        String chat(@UserMessage String query);
    }

    /**
     * Calculator tools with @Tool annotations.
     */
    public static class CalculatorTools {
        @Tool("Adds two numbers and returns the sum")
        public int add(int a, int b) {
            return a + b;
        }

        @Tool("Subtracts the second number from the first number")
        public int subtract(int a, int b) {
            return a - b;
        }

        @Tool("Multiplies two numbers and returns the product")
        public int multiply(int a, int b) {
            return a * b;
        }

        @Tool("Divides the first number by the second number")
        public double divide(double a, double b) {
            if (b == 0) {
                throw new IllegalArgumentException("Cannot divide by zero");
            }
            return a / b;
        }
    }

    /**
     * Filtered calculator tools - only exposes selected operations.
     * This demonstrates dynamic tool selection by creating a wrapper
     * that only exposes specific methods based on allowed operations.
     */
    public static class FilteredCalculatorTools {
        private final CalculatorTools calculator;
        private final List<String> allowedOperations;

        public FilteredCalculatorTools(List<String> allowedOperations) {
            this.calculator = new CalculatorTools();
            this.allowedOperations = allowedOperations;
        }

        @Tool("Adds two numbers and returns the sum")
        public int add(int a, int b) {
            if (!allowedOperations.contains("add")) {
                throw new IllegalStateException("Add operation not allowed");
            }
            return calculator.add(a, b);
        }

        @Tool("Subtracts the second number from the first number")
        public int subtract(int a, int b) {
            if (!allowedOperations.contains("subtract")) {
                throw new IllegalStateException("Subtract operation not allowed");
            }
            return calculator.subtract(a, b);
        }

        @Tool("Multiplies two numbers and returns the product")
        public int multiply(int a, int b) {
            if (!allowedOperations.contains("multiply")) {
                throw new IllegalStateException("Multiply operation not allowed");
            }
            return calculator.multiply(a, b);
        }

        @Tool("Divides the first number by the second number")
        public double divide(double a, double b) {
            if (!allowedOperations.contains("divide")) {
                throw new IllegalStateException("Divide operation not allowed");
            }
            return calculator.divide(a, b);
        }
    }

    /**
     * Calculator Assistant AI Service interface.
     */
    interface CalculatorAssistant {
        @SystemMessage("You are a helpful calculator assistant. Use the available tools to perform calculations.")
        String chat(@UserMessage String query);
    }

    /**
     * Custom Assistant AI Service interface.
     */
    interface CustomAssistant {
        @SystemMessage("You are a helpful assistant with custom tools.")
        String chat(@UserMessage String query);
    }

    /**
     * Helper method to get configuration values from system properties or environment variables.
     * Similar to AIPlugin.getConfigValue() pattern.
     */
    private static String getConfigValue(String systemProp, String... envVars) {
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

    public static void main(String[] args) {
        var app = Roya.create();

        // Register body parser middleware
        app.use(BodyParser.bodyParser());

        // Register AI plugin
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        try {
            aiPlugin.start();
        } catch (Exception e) {
            System.err.println("❌ Error: AI plugin failed to start: " + e.getMessage());
            System.exit(1);
        }

        // Calculator endpoint - Dynamically select tools based on request
        app.post("/calculator", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = req.body(Map.class);
            String query = body != null && body.containsKey("query")
                ? (String) body.get("query")
                : "What is 10 + 20?";

            @SuppressWarnings("unchecked")
            List<String> requestedTools = body != null && body.containsKey("tools")
                ? (List<String>) body.get("tools")
                : List.of("add", "subtract", "multiply", "divide"); // Default: all tools

            // Create filtered tools based on request - dynamic selection!
            FilteredCalculatorTools tools = new FilteredCalculatorTools(requestedTools);

            // Create AI Service with dynamically selected tools
            CalculatorAssistant assistant = ai.aiService(CalculatorAssistant.class, builder -> {
                builder.tools(tools); // Only selected tools are available
            });

            String response = assistant.chat(query);

            res.json(Map.of(
                "query", query,
                "response", response,
                "selectedTools", requestedTools,
                "availableTools", List.of("add", "subtract", "multiply", "divide"),
                "note", "Tools are dynamically selected based on request - only selected tools are available"
            ));
        });

        // Custom tools endpoint - Create tools from request configuration
        app.post("/custom", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = req.body(Map.class);
            String query = body != null && body.containsKey("query")
                ? (String) body.get("query")
                : "What is 5 * 3?";

            @SuppressWarnings("unchecked")
            List<String> operations = body != null && body.containsKey("operations")
                ? (List<String>) body.get("operations")
                : List.of("multiply", "divide");

            // Create filtered tools dynamically
            FilteredCalculatorTools tools = new FilteredCalculatorTools(operations);

            CustomAssistant assistant = ai.aiService(CustomAssistant.class, builder -> {
                builder.tools(tools);
            });

            String response = assistant.chat(query);

            res.json(Map.of(
                "query", query,
                "response", response,
                "operations", operations,
                "note", "Tools are created dynamically from request configuration"
            ));
        });

        // Code execution endpoint - Demonstrates Judge0 as a dynamic tool
        app.post("/code", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = req.body(Map.class);
            String query = body != null && body.containsKey("query")
                ? (String) body.get("query")
                : "Write JavaScript code to calculate the factorial of 5";

            // Check if code execution is enabled
            boolean enableCodeExecution = body != null && body.containsKey("enableCodeExecution")
                ? (Boolean) body.getOrDefault("enableCodeExecution", false)
                : true; // Default: enabled

            // Get RapidAPI key from environment variables or system properties
            String rapidApiKey = getConfigValue("rapidapi.key", "RAPIDAPI_KEY", "RAPID_API_KEY");

            if (enableCodeExecution && rapidApiKey == null) {
                res.status(400).json(Map.of(
                    "error", "RapidAPI key not configured",
                    "message", "Please set RAPIDAPI_KEY environment variable or rapidapi.key system property",
                    "note", "Get your key from https://rapidapi.com/judge0-official/api/judge0-ce"
                ));
                return;
            }

            // Dynamically add Judge0 tool based on request
            CodeExecutionAssistant assistant = ai.aiService(CodeExecutionAssistant.class, builder -> {
                if (enableCodeExecution) {
                    // Dynamically add code execution tool with RapidAPI key
                    builder.tools(new Judge0JavaScriptExecutionTool(rapidApiKey));
                }
                // Can also add calculator tools if needed
                // builder.tools(new CalculatorTools());
            });

            String response = assistant.chat(query);

            res.json(Map.of(
                "query", query,
                "response", response,
                "codeExecutionEnabled", enableCodeExecution,
                "rapidApiKeyConfigured", rapidApiKey != null,
                "note", rapidApiKey != null
                    ? "Judge0 JavaScript execution tool is dynamically added when enabled"
                    : "Judge0 tool requires RapidAPI key configuration"
            ));
        });

        // List available tools
        app.get("/tools", (req, res, next) -> {
            List<Map<String, String>> toolList = List.of(
                Map.of("name", "add", "description", "Adds two numbers and returns the sum"),
                Map.of("name", "subtract", "description", "Subtracts the second number from the first number"),
                Map.of("name", "multiply", "description", "Multiplies two numbers and returns the product"),
                Map.of("name", "divide", "description", "Divides the first number by the second number")
            );

            res.json(Map.of(
                "availableTools", toolList,
                "note", "Tools can be selected dynamically at runtime based on context, permissions, or configuration"
            ));
        });

        // Root endpoint with instructions
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "tutorial", "Tutorial 11: Dynamic Tools",
                "description", "Demonstrates selecting tools dynamically at runtime",
                "concepts", List.of(
                    "Tools can be selected/filtered at runtime",
                    "Different users can have different tool sets",
                    "Tools adapt based on permissions or context",
                    "Perfect for multi-tenant or role-based access",
                    "Enables flexible, context-aware AI assistants"
                ),
                "endpoints", Map.of(
                    "POST /calculator", "Calculator with dynamically selected tools",
                    "POST /custom", "Custom tools created from request configuration",
                    "POST /code", "Code execution with Judge0 JavaScript tool (dynamic tool)",
                    "GET /tools", "List available tools"
                ),
                "examples", List.of(
                    "curl -X POST http://localhost:3011/calculator -H 'Content-Type: application/json' -d '{\"query\":\"What is 10 + 20?\",\"tools\":[\"add\",\"multiply\"]}'",
                    "curl -X POST http://localhost:3011/custom -H 'Content-Type: application/json' -d '{\"query\":\"What is 5 * 3?\",\"operations\":[\"multiply\",\"divide\"]}'",
                    "curl -X POST http://localhost:3011/code -H 'Content-Type: application/json' -d '{\"query\":\"Calculate factorial of 5 using JavaScript\"}'",
                    "curl http://localhost:3011/tools"
                ),
                "note", "Dynamic tools enable flexible, context-aware AI assistants"
            ));
        });

        int port = 3011;
        System.out.println("🚀 Tutorial 11: Dynamic Tools");
        System.out.println("📡 Server running on http://localhost:" + port);
        System.out.println("📚 Endpoints:");
        System.out.println("   POST /calculator - Calculator with dynamically selected tools");
        System.out.println("   POST /custom - Custom tools created from request");
        System.out.println("   POST /code - Code execution with Judge0 (dynamic tool)");
        System.out.println("   GET  /tools - List available tools");
        System.out.println("   GET  / - API documentation");
        System.out.println();
        System.out.println("💡 Key Feature: Tools selected dynamically at runtime!");
        System.out.println("   Select tools based on user permissions, context, or configuration.");
        System.out.println("   Judge0 demonstrates adding code execution as a dynamic tool.");

        app.listen(port);
    }
}
