package com.akilisha.oss.roya.examples.langchain4j.mcp_github;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.tool.ToolProvider;

import java.util.List;
import java.util.Map;

/**
 * MCP GitHub Tools Example - Roya version.
 *
 * <p>This demonstrates how to use MCP (Model Context Protocol) with LangChain4j
 * to enable AI agents to interact with GitHub repositories via Docker-based MCP servers.
 *
 * <p>LangChain4j Original:
 * <pre>
 * McpTransport transport = new StdioMcpTransport.Builder()
 *     .command(List.of("docker", "run", "-i", "mcp/git"))
 *     .build();
 * 
 * McpClient mcpClient = new DefaultMcpClient.Builder()
 *     .transport(transport)
 *     .build();
 *
 * ToolProvider toolProvider = McpToolProvider.builder()
 *     .mcpClients(List.of(mcpClient))
 *     .build();
 *
 * Bot bot = AiServices.builder(Bot.class)
 *     .chatModel(model)
 *     .toolProvider(toolProvider)
 *     .build();
 * </pre>
 *
 * <p>Roya Implementation:
 * <pre>
 * AI ai = req.get(AI.class);
 *
 * // Create MCP client with Stdio transport (Docker)
 * McpTransport transport = new StdioMcpTransport.Builder()
 *     .command(List.of("docker", "run", "-i", "mcp/git"))
 *     .build();
 * 
 * McpClient mcpClient = new DefaultMcpClient.Builder()
 *     .transport(transport)
 *     .build();
 *
 * // Convert MCP tools to LangChain4j tools
 * ToolProvider toolProvider = McpToolProvider.builder()
 *     .mcpClients(List.of(mcpClient))
 *     .build();
 *
 * // Create AI Service with MCP tools
 * GitHubBot bot = ai.aiService(GitHubBot.class, builder -> {
 *     builder.toolProvider(toolProvider);
 * });
 * </pre>
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Stdio transport - communicates with Docker-based MCP servers via stdin/stdout</li>
 *   <li>Automatic tool discovery - MCP tools are automatically discovered and converted</li>
 *   <li>AI Service integration - use MCP tools with LangChain4j AI Services</li>
 *   <li>No authentication required - works with public GitHub repositories</li>
 * </ul>
 *
 * <p>HTTP Endpoints:
 * <ul>
 *   <li>{@code POST /summarize-commits} - Summarize commits using MCP GitHub tools</li>
 *   <li>{@code GET /} - API documentation</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>Docker must be installed and running</li>
 *   <li>GitHub MCP Docker image must be built: {@code docker build -t mcp/git .}</li>
 *   <li>See: https://github.com/modelcontextprotocol/servers/tree/main/src/git</li>
 *   <li>OPENAI_API_KEY must be configured</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * curl -X POST http://localhost:3014/summarize-commits \
 *   -H "Content-Type: application/json" \
 *   -d '{"question": "Summarize the last 3 commits of the LangChain4j GitHub repository"}'
 * </pre>
 */
public class McpGithubExample {

    /**
     * GitHub Bot AI Service interface.
     * Uses MCP tools to interact with GitHub repositories.
     */
    interface GitHubBot {
        @SystemMessage("You are a helpful assistant that can interact with GitHub repositories using MCP tools. " +
                "Use the available tools to answer questions about GitHub repositories, commits, and related information.")
        String chat(@UserMessage String question);
    }

    /**
     * Create an MCP client with Stdio transport for Docker-based GitHub MCP server.
     * 
     * @param dockerImage Docker image name (default: "mcp/git")
     * @param githubToken Optional GitHub personal access token (for private repos)
     * @return Configured MCP client
     */
    private static McpClient createGitHubMcpClient(String dockerImage, String githubToken) {
        List<String> command;
        
        // Add GitHub token as environment variable if provided
        if (githubToken != null && !githubToken.isEmpty()) {
            command = List.of(
                "docker", "run", "-e", "GITHUB_PERSONAL_ACCESS_TOKEN=" + githubToken, "-i", dockerImage
            );
        } else {
            command = List.of(
                "docker", "run", "-i", dockerImage
            );
        }
        
        McpTransport transport = new StdioMcpTransport.Builder()
            .command(command)
            .logEvents(true)
            .build();
        
        return new DefaultMcpClient.Builder()
            .transport(transport)
            .build();
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

        // Summarize commits endpoint
        app.post("/summarize-commits", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = req.body(Map.class);
            String question = body != null && body.containsKey("question")
                ? (String) body.get("question")
                : "Summarize the last 3 commits of the LangChain4j GitHub repository";

            String dockerImage = body != null && body.containsKey("dockerImage")
                ? (String) body.get("dockerImage")
                : "mcp/git"; // Default GitHub MCP Docker image

            String githubToken = body != null && body.containsKey("githubToken")
                ? (String) body.get("githubToken")
                : System.getenv("GITHUB_PERSONAL_ACCESS_TOKEN"); // Optional, for private repos

            McpClient mcpClient = null;
            try {
                // Create MCP client with Stdio transport (Docker)
                mcpClient = createGitHubMcpClient(dockerImage, githubToken);

                // Convert MCP tools to LangChain4j ToolProvider
                ToolProvider toolProvider = McpToolProvider.builder()
                    .mcpClients(List.of(mcpClient))
                    .build();

                // Create AI Service with MCP tools
                GitHubBot bot = ai.aiService(GitHubBot.class, builder -> {
                    builder.toolProvider(toolProvider);
                });

                // Use the bot to answer the question
                String response = bot.chat(question);

                res.json(Map.of(
                    "question", question,
                    "response", response,
                    "dockerImage", dockerImage,
                    "note", "MCP tools were used to interact with GitHub repositories"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "error", "Failed to process request",
                    "message", e.getMessage(),
                    "note", "Make sure Docker is running and the mcp/git image is built. " +
                            "See: https://github.com/modelcontextprotocol/servers/tree/main/src/git"
                ));
            } finally {
                // Cleanup: close MCP client
                if (mcpClient != null) {
                    try {
                        mcpClient.close();
                    } catch (Exception e) {
                        System.err.println("Error closing MCP client: " + e.getMessage());
                    }
                }
            }
        });

        // Root endpoint with instructions
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "application", "MCP GitHub Tools Example",
                "description", "Demonstrates MCP (Model Context Protocol) integration with GitHub",
                "features", List.of(
                    "Stdio transport - communicates with Docker-based MCP servers",
                    "Automatic tool discovery - MCP tools converted to LangChain4j tools",
                    "GitHub integration - interact with repositories, commits, issues",
                    "AI-powered - uses LLM to process and summarize GitHub data"
                ),
                "endpoints", Map.of(
                    "POST /summarize-commits", "Summarize commits using MCP GitHub tools"
                ),
                "examples", List.of(
                    "curl -X POST http://localhost:3014/summarize-commits -H 'Content-Type: application/json' -d '{\"question\":\"Summarize the last 3 commits of the LangChain4j GitHub repository\"}'"
                ),
                "prerequisites", Map.of(
                    "Docker", "Must be installed and running",
                    "MCP Docker Image", "Build with: docker build -t mcp/git . (see https://github.com/modelcontextprotocol/servers/tree/main/src/git)",
                    "OPENAI_API_KEY", "Required for LLM",
                    "GITHUB_PERSONAL_ACCESS_TOKEN", "Optional (for private repositories)"
                ),
                "note", "MCP enables AI agents to discover and use tools from external servers"
            ));
        });

        int port = 3014;
        System.out.println("🔧 MCP GitHub Tools Example");
        System.out.println("📡 Server running on http://localhost:" + port);
        System.out.println("📚 Endpoints:");
        System.out.println("   POST /summarize-commits - Summarize commits using MCP GitHub tools");
        System.out.println("   GET  / - API documentation");
        System.out.println();
        System.out.println("💡 Key Feature: MCP (Model Context Protocol) Integration!");
        System.out.println("   - Stdio transport connects to Docker-based MCP servers");
        System.out.println("   - Automatic tool discovery and conversion");
        System.out.println("   - AI agents can interact with GitHub repositories");
        System.out.println();
        System.out.println("📋 Prerequisites:");
        System.out.println("   1. Docker must be installed and running");
        System.out.println("   2. Build GitHub MCP Docker image:");
        System.out.println("      git clone https://github.com/modelcontextprotocol/servers.git");
        System.out.println("      cd servers/src/git");
        System.out.println("      docker build -t mcp/git .");
        System.out.println("   3. OPENAI_API_KEY must be configured");
        System.out.println("   4. (Optional) GITHUB_PERSONAL_ACCESS_TOKEN for private repos");

        app.listen(port);
    }
}

