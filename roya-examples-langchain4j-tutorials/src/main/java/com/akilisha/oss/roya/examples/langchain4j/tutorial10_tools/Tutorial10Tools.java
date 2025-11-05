package com.akilisha.oss.roya.examples.langchain4j.tutorial10_tools;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;
import java.util.Map;

/**
 * Tutorial 10: Tools/Function Calling
 *
 * <p>Recreates LangChain4j's tools/function calling tutorial.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>Defining tools as Java methods with @Tool annotation</li>
 *   <li>Creating AI Services that can use tools</li>
 *   <li>LLM automatically decides when to call tools</li>
 *   <li>Tool execution and result integration</li>
 *   <li>Multi-step reasoning with tool usage</li>
 * </ul>
 *
 * <p>LangChain4j Approach:
 * <pre>
 * // Define tools as methods with @Tool annotation
 * public class CalculatorTools {
 *     {@code @Tool("Adds two numbers")}
 *     public int add(int a, int b) {
 *         return a + b;
 *     }
 * }
 *
 * // Create AI Service with tools
 * Assistant assistant = AiServices.builder(Assistant.class)
 *     .chatModel(chatModel)
 *     .tools(new CalculatorTools())
 *     .build();
 *
 * // LLM automatically uses tools when needed
 * String result = assistant.chat("What is 15 + 27?");
 * </pre>
 *
 * <p>Roya Approach:
 * <pre>
 * AI ai = req.get(AI.class);
 *
 * // Create AI Service with tools
 * CalculatorAssistant assistant = ai.aiService(CalculatorAssistant.class, builder -> {
 *     builder.tools(List.of(new CalculatorTools()));
 * });
 *
 * String result = assistant.chat("What is 15 + 27?");
 * </pre>
 *
 * <p><b>Key Points:</b>
 * <ul>
 *   <li>Tools are Java methods annotated with {@code @Tool}</li>
 *   <li>LLM automatically decides when to call tools based on context</li>
 *   <li>Tool descriptions help LLM understand when to use them</li>
 *   <li>Multiple tools can be provided to an AI Service</li>
 *   <li>Tool results are automatically integrated into the conversation</li>
 * </ul>
 *
 * <p>HTTP Endpoints:
 * <ul>
 *   <li>{@code POST /calculator} - Calculator with math tools</li>
 *   <li>{@code POST /weather} - Weather assistant with weather tool</li>
 *   <li>{@code POST /research} - Research assistant with multiple tools</li>
 *   <li>{@code GET /} - Root endpoint with instructions</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * // Calculator example
 * curl -X POST http://localhost:3010/calculator \
 *   -H "Content-Type: application/json" \
 *   -d '{"query": "What is 123 + 456?"}'
 *
 * // Weather example
 * curl -X POST http://localhost:3010/weather \
 *   -H "Content-Type: application/json" \
 *   -d '{"query": "What is the weather in New York?"}'
 * </pre>
 */
public class Tutorial10Tools {

    /**
     * Calculator tools for mathematical operations.
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
     * Weather tools for weather information.
     */
    public static class WeatherTools {
        @Tool("Gets the current weather for a given city")
        public String getWeather(String city) {
            // Simulate weather data (in real app, call a weather API)
            return switch (city.toLowerCase()) {
                case "new york", "nyc" -> "Sunny, 72°F (22°C)";
                case "london" -> "Cloudy, 60°F (15°C)";
                case "tokyo" -> "Rainy, 68°F (20°C)";
                case "paris" -> "Partly cloudy, 65°F (18°C)";
                default -> "Weather data not available for " + city;
            };
        }

        @Tool("Converts temperature from Fahrenheit to Celsius")
        public double fahrenheitToCelsius(double fahrenheit) {
            return (fahrenheit - 32) * 5.0 / 9.0;
        }
    }

    /**
     * Research tools for information gathering.
     */
    public static class ResearchTools {
        @Tool("Searches the web for information about a topic")
        public String searchWeb(String query) {
            // Simulate web search (in real app, call a search API)
            return "Search results for '" + query + "': Found relevant information about " + query.toLowerCase();
        }

        @Tool("Gets the current date and time")
        public String getCurrentDateTime() {
            return java.time.LocalDateTime.now().toString();
        }

        @Tool("Calculates the square root of a number")
        public double sqrt(double number) {
            return Math.sqrt(number);
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
     * Weather Assistant AI Service interface.
     */
    interface WeatherAssistant {
        @SystemMessage("You are a helpful weather assistant. Use the available tools to get weather information.")
        String chat(@UserMessage String query);
    }

    /**
     * Research Assistant AI Service interface.
     */
    interface ResearchAssistant {
        @SystemMessage("You are a helpful research assistant. Use the available tools to gather information.")
        String chat(@UserMessage String query);
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

        // Calculator endpoint - Uses CalculatorTools
        app.post("/calculator", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, String> body = req.body(Map.class);
            String query = body != null && body.containsKey("query")
                ? body.get("query")
                : "What is 10 + 20?";

            // Create AI Service with calculator tools (type-safe, no reflection!)
            CalculatorAssistant assistant = ai.aiService(CalculatorAssistant.class, builder -> {
                builder.tools(new CalculatorTools());
            });

            String response = assistant.chat(query);

            res.json(Map.of(
                "query", query,
                "response", response,
                "tools", List.of("add", "subtract", "multiply", "divide"),
                "note", "LLM automatically decides when to use calculator tools"
            ));
        });

        // Weather endpoint - Uses WeatherTools
        app.post("/weather", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, String> body = req.body(Map.class);
            String query = body != null && body.containsKey("query")
                ? body.get("query")
                : "What is the weather in New York?";

            // Create AI Service with weather tools
            WeatherAssistant assistant = ai.aiService(WeatherAssistant.class, builder -> {
                builder.tools(new WeatherTools());
            });

            String response = assistant.chat(query);

            res.json(Map.of(
                "query", query,
                "response", response,
                "tools", List.of("getWeather", "fahrenheitToCelsius"),
                "note", "LLM automatically uses weather tools when needed"
            ));
        });

        // Research endpoint - Uses ResearchTools
        app.post("/research", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, String> body = req.body(Map.class);
            String query = body != null && body.containsKey("query")
                ? body.get("query")
                : "What is the square root of 144?";

            // Create AI Service with research tools
            ResearchAssistant assistant = ai.aiService(ResearchAssistant.class, builder -> {
                builder.tools(new ResearchTools());
            });

            String response = assistant.chat(query);

            res.json(Map.of(
                "query", query,
                "response", response,
                "tools", List.of("searchWeb", "getCurrentDateTime", "sqrt"),
                "note", "LLM can use multiple tools in sequence for complex queries"
            ));
        });

        // Root endpoint with instructions
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "tutorial", "Tutorial 10: Tools/Function Calling",
                "description", "Demonstrates LLM function calling with tools using LangChain4j",
                "concepts", List.of(
                    "Tools are Java methods annotated with @Tool",
                    "LLM automatically decides when to call tools",
                    "Tool descriptions help LLM understand usage",
                    "Multiple tools can be provided to AI Services",
                    "Tool results are automatically integrated"
                ),
                "endpoints", Map.of(
                    "POST /calculator", "Calculator assistant with math tools (add, subtract, multiply, divide)",
                    "POST /weather", "Weather assistant with weather tools (getWeather, fahrenheitToCelsius)",
                    "POST /research", "Research assistant with multiple tools (searchWeb, getCurrentDateTime, sqrt)"
                ),
                "examples", List.of(
                    "curl -X POST http://localhost:3010/calculator -H 'Content-Type: application/json' -d '{\"query\":\"What is 123 + 456?\"}'",
                    "curl -X POST http://localhost:3010/weather -H 'Content-Type: application/json' -d '{\"query\":\"What is the weather in London?\"}'",
                    "curl -X POST http://localhost:3010/research -H 'Content-Type: application/json' -d '{\"query\":\"What is sqrt(144)?\"}'"
                ),
                "note", "Tools allow LLMs to interact with external systems and perform computations"
            ));
        });

        int port = 3010;
        System.out.println("🚀 Tutorial 10: Tools/Function Calling");
        System.out.println("📡 Server running on http://localhost:" + port);
        System.out.println("📚 Endpoints:");
        System.out.println("   POST /calculator - Calculator assistant with math tools");
        System.out.println("   POST /weather - Weather assistant with weather tools");
        System.out.println("   POST /research - Research assistant with multiple tools");
        System.out.println("   GET  / - API documentation");
        System.out.println();
        System.out.println("💡 Key Feature: LLM automatically decides when to use tools!");
        System.out.println("   Ask complex questions and watch the LLM call tools as needed.");

        app.listen(port);
    }
}

