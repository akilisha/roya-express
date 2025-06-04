// src/main/java/com/example/webapp/App.java
package com.example.webapp;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jboss.weld.environment.servlet.Listener; // Weld listener for CDI

/**
* Main class to embed and start the Jetty server.
* This class configures Jetty to host a JAX-RS (Jersey) application
* with CDI (Weld) for dependency injection.
  */
  public class App {

  public static void main(String[] args) throws Exception {
  // Create a new Jetty server instance on port 8080
  Server server = new Server(8080);

       // Create a ServletContextHandler to configure the web application context
       ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
       context.setContextPath("/"); // Set the context path to the root
       server.setHandler(context); // Set the context handler for the server

       // Add the Weld listener to the servlet context.
       // This listener initializes the CDI container when the web application starts.
       context.addEventListener(new Listener());

       // Configure Jersey (JAX-RS) servlet
       ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/api/*");
       jerseyServlet.setInitOrder(0); // Ensure Jersey servlet is initialized first

       // Tell Jersey where to find our REST API resources
       jerseyServlet.setInitParameter(
               "jersey.config.server.provider.packages",
               "com.example.webapp.resource" // Package containing JAX-RS resources
       );
       // Register the authentication filter globally for all Jersey resources
       jerseyServlet.setInitParameter(
               "jersey.config.server.provider.classnames",
               "com.example.webapp.filter.AuthFilter"
       );

       // Start the Jetty server
       try {
           server.start();
           server.join(); // Wait for the server to finish
       } catch (Exception e) {
           e.printStackTrace();
           server.destroy(); // Destroy the server in case of an error
       }
  }
  }
```java
// src/main/java/com/example/webapp/service/AuthService.java
package com.example.webapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.logging.Logger;

/**
 * Service for handling OAuth authentication.
 * This is a conceptual implementation as full OAuth flows require
 * redirection, client-side interaction, and secure token storage,
 * which are beyond the scope of a simple server-side code example.
 *
 * It would typically involve:
 * 1. Redirecting the user to the OAuth provider's authorization endpoint.
 * 2. The user grants permission.
 * 3. The OAuth provider redirects back to a configured callback URL on this application.
 * 4. This application exchanges the authorization code for an access token.
 * 5. The access token is then used to access protected resources on the OAuth provider
 * or to establish an authenticated session for the user within this application.
 */
@ApplicationScoped // Makes this a CDI managed bean, available throughout the application
@Named("authService") // Optional: gives it a name for injection if needed by name
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    // --- Configuration for Okta OAuth provider ---
    // IMPORTANT: Replace {yourOktaDomain} with your actual Okta domain (e.g., dev-123456.okta.com)
    private static final String OAUTH_CLIENT_ID = "your_okta_client_id";
    private static final String OAUTH_CLIENT_SECRET = "your_okta_client_secret"; // Keep secure!
    private static final String OAUTH_AUTHORIZATION_URL = "https://{yourOktaDomain}/oauth2/default/v1/authorize";
    private static final String OAUTH_TOKEN_URL = "https://{yourOktaDomain}/oauth2/default/v1/token";
    private static final String OAUTH_REDIRECT_URI = "http://localhost:8080/api/auth/callback"; // Must be configured in your Okta application
    private static final String OAUTH_SCOPE = "openid profile email"; // Common Okta scopes

    /**
     * Initiates the OAuth authentication flow.
     * In a real application, this would return a redirect URL to the client.
     *
     * @return The URL to redirect the user to for OAuth authorization.
     */
    public String initiateOAuthFlow() {
        LOGGER.info("Initiating OAuth flow...");
        // Construct the authorization URL
        return String.format("%s?response_type=code&client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                OAUTH_AUTHORIZATION_URL, OAUTH_CLIENT_ID, OAUTH_REDIRECT_URI, OAUTH_SCOPE, generateState());
    }

    /**
     * Handles the OAuth callback, exchanging the authorization code for an access token.
     * This method would typically be called by a JAX-RS endpoint that receives the callback.
     *
     * @param authorizationCode The authorization code received from the OAuth provider.
     * @param state The state parameter for CSRF protection.
     * @return A conceptual access token or null if authentication fails.
     */
    public String handleOAuthCallback(String authorizationCode, String state) {
        LOGGER.info("Handling OAuth callback with code: " + authorizationCode);
        // In a real scenario, validate the 'state' parameter to prevent CSRF attacks.
        // Then, make an HTTP POST request to the OAUTH_TOKEN_URL to exchange the code for a token.

        // Example of a simulated token exchange:
        if (authorizationCode != null && !authorizationCode.isEmpty() /* && validateState(state) */) {
            LOGGER.info("Simulating token exchange for code: " + authorizationCode);
            // Here, you would typically use an HTTP client to call the token endpoint.
            // HttpClient httpClient = HttpClient.newHttpClient();
            // HttpRequest request = HttpRequest.newBuilder()
            //     .uri(URI.create(OAUTH_TOKEN_URL))
            //     .header("Content-Type", "application/x-www-form-urlencoded")
            //     .POST(HttpRequest.BodyPublishers.ofString(
            //         "grant_type=authorization_code&client_id=" + OAUTH_CLIENT_ID +
            //         "&client_secret=" + OAUTH_CLIENT_SECRET +
            //         "&redirect_uri=" + OAUTH_REDIRECT_URI +
            //         "&code=" + authorizationCode
            //     ))
            //     .build();
            // HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            // Parse response to get access token, refresh token, etc.

            // For demonstration, return a dummy token
            return "dummy_access_token_for_" + authorizationCode;
        }
        LOGGER.warn("OAuth callback failed: invalid code or state.");
        return null;
    }

    /**
     * Validates an access token. In a real application, this might involve:
     * - Checking token expiration.
     * - Calling the OAuth provider's introspection/userinfo endpoint.
     * - Validating JWT signatures if it's an OIDC ID Token.
     *
     * @param accessToken The access token to validate.
     * @return True if the token is valid, false otherwise.
     */
    public boolean validateAccessToken(String accessToken) {
        LOGGER.info("Validating access token: " + accessToken);
        // Simulate token validation
        return accessToken != null && accessToken.startsWith("dummy_access_token");
    }

    /**
     * Generates a random state string for CSRF protection.
     * @return A random string.
     */
    private String generateState() {
        return java.util.UUID.randomUUID().toString();
    }
}
```java
// src/main/java/com/example/webapp/service/GenerativeAIService.java
package com.example.webapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * Service for interacting with a conceptual Generative AI backend.
 * This service would make HTTP requests to an external AI API
 * for tasks like sentiment analysis, political ideology ranking, and summarization.
 *
 * For demonstration, the actual API calls are simulated, but the structure
 * for making such calls using HttpClient is provided.
 */
@ApplicationScoped
public class GenerativeAIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerativeAIService.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // --- Configuration for ChatGPT API ---
    // IMPORTANT: Replace with your actual OpenAI API Key.
    // In a real application, this would be loaded from environment variables or a secure configuration.
    private static final String OPENAI_API_KEY = "your_openai_api_key";
    private static final String CHATGPT_API_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private static final String CHATGPT_MODEL = "gpt-3.5-turbo"; // Or "gpt-4", etc.

    public GenerativeAIService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Performs sentiment analysis on the given content using the ChatGPT API.
     *
     * @param content The text content to analyze.
     * @return A string representing the sentiment (e.g., "positive", "negative", "neutral").
     */
    public String analyzeSentiment(String content) {
        LOGGER.info("Analyzing sentiment for content: " + content.substring(0, Math.min(content.length(), 100)) + "...");
        try {
            String systemPrompt = "You are a sentiment analysis expert. Analyze the sentiment of the following text and classify it as positive, negative, or neutral. Respond with only the sentiment word (positive, negative, or neutral).";
            String userPrompt = "Text: \"" + content + "\"";

            String simulatedResponse = callChatGPTApiSimulated(systemPrompt, userPrompt);

            JsonNode rootNode = objectMapper.readTree(simulatedResponse);
            String sentiment = rootNode.path("choices").path(0).path("message").path("content").asText();

            // Simple logic to extract sentiment from the AI's response
            if (sentiment.toLowerCase().contains("positive")) {
                return "positive";
            } else if (sentiment.toLowerCase().contains("negative")) {
                return "negative";
            } else {
                return "neutral";
            }
        } catch (Exception e) {
            LOGGER.error("Error analyzing sentiment: " + e.getMessage());
            return "unknown"; // Default to unknown on error
        }
    }

    /**
     * Ranks content based on political ideology using the ChatGPT API.
     *
     * @param content The text content to rank.
     * @return A string representing the political ideology (e.g., "left-leaning", "right-leaning", "centrist").
     */
    public String rankPoliticalIdeology(String content) {
        LOGGER.info("Ranking political ideology for content: " + content.substring(0, Math.min(content.length(), 100)) + "...");
        try {
            String systemPrompt = "You are an expert in political science. Based on the following text, identify the predominant political ideology (e.g., left-leaning, right-leaning, centrist, libertarian, socialist, etc.). Respond with only the ideology term.";
            String userPrompt = "Text: \"" + content + "\"";

            String simulatedResponse = callChatGPTApiSimulated(systemPrompt, userPrompt);

            JsonNode rootNode = objectMapper.readTree(simulatedResponse);
            String ideology = rootNode.path("choices").path(0).path("message").path("content").asText();

            // Simple logic to extract ideology from the AI's response
            if (ideology.toLowerCase().contains("left-leaning")) {
                return "left-leaning";
            } else if (ideology.toLowerCase().contains("right-leaning")) {
                return "right-leaning";
            } else if (ideology.toLowerCase().contains("centrist")) {
                return "centrist";
            } else {
                return "unknown"; // Default to unknown
            }
        } catch (Exception e) {
            LOGGER.error("Error ranking political ideology: " + e.getMessage());
            return "unknown"; // Default to unknown on error
        }
    }

    /**
     * Summarizes the given content to 512 words or less using the ChatGPT API.
     *
     * @param content The text content to summarize.
     * @return The summarized content.
     */
    public String summarizeContent(String content) {
        LOGGER.info("Summarizing content: " + content.substring(0, Math.min(content.length(), 100)) + "...");
        try {
            String systemPrompt = "You are a professional summarizer. Summarize the following text in 512 words or less. Focus on the main points and key takeaways.";
            String userPrompt = "Text: \"" + content + "\"";

            String simulatedResponse = callChatGPTApiSimulated(systemPrompt, userPrompt);

            JsonNode rootNode = objectMapper.readTree(simulatedResponse);
            String summary = rootNode.path("choices").path(0).path("message").path("content").asText();

            return summary;
        } catch (Exception e) {
            LOGGER.error("Error summarizing content: " + e.getMessage());
            return "Error summarizing content.";
        }
    }

    /**
     * Simulates a call to the ChatGPT API.
     * In a real application, this would involve sending an HTTP request
     * to the actual API endpoint and handling the response.
     *
     * @param systemPrompt The system role prompt for the AI.
     * @param userPrompt The user role prompt for the AI.
     * @return A simulated JSON response from the ChatGPT API.
     * @throws IOException If there's an issue with JSON processing.
     * @throws InterruptedException If the HTTP request is interrupted.
     */
    private String callChatGPTApiSimulated(String systemPrompt, String userPrompt) throws IOException, InterruptedException {
        // Build the request payload for ChatGPT API
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", CHATGPT_MODEL);

        ArrayNode messagesArray = objectMapper.createArrayNode();
        messagesArray.add(objectMapper.createObjectNode().put("role", "system").put("content", systemPrompt));
        messagesArray.add(objectMapper.createObjectNode().put("role", "user").put("content", userPrompt));
        payload.set("messages", messagesArray);

        // Optional: Add max_tokens, temperature, etc.
        // payload.put("max_tokens", 512);
        // payload.put("temperature", 0.7);

        String requestBody = objectMapper.writeValueAsString(payload);

        // Construct the HTTP request
        // In a real scenario, you would uncomment and use this:
        // HttpRequest request = HttpRequest.newBuilder()
        //         .uri(URI.create(CHATGPT_API_ENDPOINT))
        //         .header("Content-Type", "application/json")
        //         .header("Authorization", "Bearer " + OPENAI_API_KEY)
        //         .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        //         .build();
        // HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        // String responseBody = response.body();
        // return responseBody;

        // For demonstration, return a dummy response based on the prompt
        String dummyContentResponse;
        if (systemPrompt.contains("sentiment")) {
            if (userPrompt.toLowerCase().contains("positive")) {
                dummyContentResponse = "positive";
            } else if (userPrompt.toLowerCase().contains("negative")) {
                dummyContentResponse = "negative";
            } else {
                dummyContentResponse = "neutral";
            }
        } else if (systemPrompt.contains("political ideology")) {
            if (userPrompt.toLowerCase().contains("economy")) {
                dummyContentResponse = "centrist";
            } else if (userPrompt.toLowerCase().contains("social justice")) {
                dummyContentResponse = "left-leaning";
            } else {
                dummyContentResponse = "right-leaning";
            }
        } else if (systemPrompt.contains("summarize")) {
            dummyContentResponse = "This is a simulated summary generated by a ChatGPT-like model, adhering to the 512-word limit. It captures the core essence and key points of the original text, demonstrating the summarization capability. The purpose is to provide a concise overview, making it easier to digest large volumes of information from web scans. This simulated output illustrates how such a model would condense complex articles into actionable insights, focusing on relevance and brevity for efficient consumption.";
        } else {
            dummyContentResponse = "This is a generic simulated response from the ChatGPT API for the given prompt.";
        }

        // Construct a JSON response similar to what ChatGPT API might return
        ObjectNode simulatedJson = objectMapper.createObjectNode();
        simulatedJson.put("id", "chatcmpl-simulated");
        simulatedJson.put("object", "chat.completion");
        simulatedJson.put("created", System.currentTimeMillis() / 1000);
        simulatedJson.put("model", CHATGPT_MODEL);
        simulatedJson.put("system_fingerprint", "fp_simulated");

        ArrayNode choicesArray = objectMapper.createArrayNode();
        ObjectNode choice = objectMapper.createObjectNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "assistant");
        message.put("content", dummyContentResponse);
        choice.set("message", message);
        choice.put("finish_reason", "stop");
        choice.put("index", 0);
        choicesArray.add(choice);
        simulatedJson.set("choices", choicesArray);

        ObjectNode usage = objectMapper.createObjectNode();
        usage.put("prompt_tokens", 50); // Simulated
        usage.put("completion_tokens", 100); // Simulated
        usage.put("total_tokens", 150); // Simulated
        simulatedJson.set("usage", usage);

        return objectMapper.writeValueAsString(simulatedJson);
    }
}
```java
// src/main/java/com/example/webapp/service/WebScannerService.java
package com.example.webapp.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.example.webapp.model.ScanResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service for periodically scanning the web for content based on keywords.
 * This service uses a scheduled executor to perform scans and
 * integrates with the GenerativeAIService for analysis.
 *
 * Note: Actual web scraping can be resource-intensive and requires
 * careful handling of website terms of service and rate limits.
 * This implementation is for demonstration purposes.
 */
@ApplicationScoped
public class WebScannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebScannerService.class);

    @Inject // CDI will inject an instance of GenerativeAIService
    private GenerativeAIService generativeAIService;

    private ScheduledExecutorService scheduler;
    private List<String> keywords = new ArrayList<>(List.of("AI ethics", "data privacy", "future of work"));
    private List<ScanResult> latestScanResults = Collections.synchronizedList(new ArrayList<>());

    /**
     * Initializes the service after construction.
     * Sets up the periodic web scanning task.
     */
    @PostConstruct
    public void init() {
        LOGGER.info("WebScannerService initialized. Starting periodic scan task.");
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Schedule the scan task to run every 10 minutes (for demonstration)
        scheduler.scheduleAtFixedRate(this::performScan, 0, 10, TimeUnit.MINUTES);
    }

    /**
     * Cleans up resources before the application shuts down.
     * Shuts down the scheduled executor service.
     */
    @PreDestroy
    public void destroy() {
        LOGGER.info("WebScannerService shutting down. Stopping periodic scan task.");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow(); // Attempt to stop all actively executing tasks
            try {
                // Wait a while for tasks to terminate
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOGGER.warn("Scheduler did not terminate in time.");
                }
            } catch (InterruptedException e) {
                LOGGER.error("Scheduler shutdown interrupted: " + e.getMessage());
                Thread.currentThread().interrupt(); // Restore the interrupted status
            }
        }
    }

    /**
     * Performs the actual web scanning operation.
     * This method simulates searching for content and then processing it.
     */
    private void performScan() {
        LOGGER.info("Performing web scan with keywords: " + keywords);
        List<ScanResult> currentScanResults = new ArrayList<>();

        // Simulate searching for content based on keywords.
        // In a real application, this would involve calling a search engine API
        // or performing targeted crawling.
        List<String> simulatedUrls = List.of(
                "[https://example.com/article-about-ai-ethics-1](https://example.com/article-about-ai-ethics-1)",
                "[https://example.com/blog-on-data-privacy-trends](https://example.com/blog-on-data-privacy-trends)",
                "[https://example.com/report-on-future-of-work-automation](https://example.com/report-on-future-of-work-automation)",
                "[https://example.com/opinion-piece-on-political-bias-in-media](https://example.com/opinion-piece-on-political-bias-in-media)",
                "[https://example.com/tech-news-on-new-regulations](https://example.com/tech-news-on-new-regulations)",
                "[https://example.com/another-ai-ethics-discussion](https://example.com/another-ai-ethics-discussion)",
                "[https://example.com/privacy-advocacy-group](https://example.com/privacy-advocacy-group)",
                "[https://example.com/economic-impact-of-automation](https://example.com/economic-impact-of-automation)",
                "[https://example.com/social-commentary-politics](https://example.com/social-commentary-politics)",
                "[https://example.com/environmental-policy-debate](https://example.com/environmental-policy-debate)",
                "[https://example.com/global-health-initiatives](https://example.com/global-health-initiatives)",
                "[https://example.com/education-reform-ideas](https://example.com/education-reform-ideas)",
                "[https://example.com/urban-development-projects](https://example.com/urban-development-projects)",
                "[https://example.com/cultural-trends-analysis](https://example.com/cultural-trends-analysis)",
                "[https://example.com/financial-market-outlook](https://example.com/financial-market-outlook)",
                "[https://example.com/sports-analytics-insights](https://example.com/sports-analytics-insights)",
                "[https://example.com/scientific-discoveries](https://example.com/scientific-discoveries)",
                "[https://example.com/historical-perspectives](https://example.com/historical-perspectives)",
                "[https://example.com/travel-destination-guides](https://example.com/travel-destination-guides)",
                "[https://example.com/food-and-nutrition-tips](https://example.com/food-and-nutrition-tips)"
        );

        for (String url : simulatedUrls) {
            try {
                // Simulate fetching content using Jsoup
                // In a real scenario, you'd connect to the actual URL
                // Document doc = Jsoup.connect(url).get();
                // String content = doc.body().text(); // Extract all text from the body

                // For demonstration, use dummy content
                String dummyContent = generateDummyContent(url);
                String author = extractAuthorFromUrl(url); // Simulate author extraction

                // Pass content to generative AI for analysis
                String sentiment = generativeAIService.analyzeSentiment(dummyContent);
                String ideology = generativeAIService.rankPoliticalIdeology(dummyContent);
                String summary = generativeAIService.summarizeContent(dummyContent);

                ScanResult result = new ScanResult(url, dummyContent, sentiment, ideology, summary, author);
                currentScanResults.add(result);
                LOGGER.info("Processed URL: " + url + ", Sentiment: " + sentiment + ", Ideology: " + ideology);

            } catch (Exception e) {
                LOGGER.warn("Failed to process URL " + url + ": " + e.getMessage());
            }
        }

        // Sort results (e.g., by a custom relevance score, or just take top 20 alphabetically for demo)
        // In a real application, you'd have a more sophisticated ranking based on AI analysis.
        currentScanResults.sort((r1, r2) -> r1.getUrl().compareTo(r2.getUrl()));

        // Pick the top 20 results
        List<ScanResult> top20Results = currentScanResults.stream()
                .limit(20)
                .collect(Collectors.toList());

        // Update the latest scan results (thread-safe operation)
        synchronized (latestScanResults) {
            latestScanResults.clear();
            latestScanResults.addAll(top20Results);
        }
        LOGGER.info("Scan completed. " + top20Results.size() + " top results stored.");
    }

    /**
     * Generates dummy content for a given URL.
     * In a real scenario, this would be the actual scraped content.
     */
    private String generateDummyContent(String url) {
        if (url.contains("ai-ethics")) {
            return "This article discusses the ethical implications of artificial intelligence, focusing on bias in algorithms, data privacy concerns, and the need for responsible AI development. It highlights the challenges of ensuring fairness and transparency in AI systems and proposes frameworks for ethical AI governance. The author argues that strong regulations are needed to prevent misuse and ensure societal benefit.";
        } else if (url.contains("data-privacy")) {
            return "A deep dive into current data privacy trends, including new regulations like GDPR and CCPA, the rise of privacy-enhancing technologies, and the ongoing debate about user consent and data ownership. It examines how companies are adapting to these changes and the impact on consumer trust. The article emphasizes the importance of robust data protection measures.";
        } else if (url.contains("future-of-work")) {
            return "An analysis of how automation and AI are reshaping the future of work, discussing job displacement, the emergence of new roles, and the need for reskilling the workforce. It explores the potential for increased productivity and economic growth, alongside concerns about social inequality and the changing nature of employment. The author suggests proactive policy measures.";
        } else if (url.contains("political-bias")) {
            return "This opinion piece argues that mainstream media exhibits significant political bias, influencing public discourse and electoral outcomes. It provides examples of how certain narratives are amplified or suppressed, leading to a polarized society. The author advocates for media literacy and diverse news consumption to counteract these biases.";
        } else if (url.contains("social-commentary-politics")) {
            return "A commentary on contemporary political movements and their impact on society. It explores the dynamics of grassroots activism, the role of social media in political mobilization, and the challenges of achieving consensus in a diverse democratic landscape. The author reflects on historical precedents and future implications.";
        }
        return "Generic content for " + url + ". This content is a placeholder for actual web-scraped data. It would normally be parsed and analyzed by the generative AI for sentiment and ideology.";
    }

    /**
     * Simulates extracting the author from a URL.
     * In a real scenario, this would involve parsing the HTML content for author metadata.
     */
    private String extractAuthorFromUrl(String url) {
        if (url.contains("[example.com/article-about-ai-ethics-1](https://example.com/article-about-ai-ethics-1)")) return "Dr. Alice Smith";
        if (url.contains("[example.com/blog-on-data-privacy-trends](https://example.com/blog-on-data-privacy-trends)")) return "Bob Johnson";
        if (url.contains("[example.com/report-on-future-of-work-automation](https://example.com/report-on-future-of-work-automation)")) return "Dr. Carol White";
        if (url.contains("[example.com/opinion-piece-on-political-bias-in-media](https://example.com/opinion-piece-on-political-bias-in-media)")) return "David Green";
        if (url.contains("[example.com/tech-news-on-new-regulations](https://example.com/tech-news-on-new-regulations)")) return "Eve Black";
        if (url.contains("[example.com/another-ai-ethics-discussion](https://example.com/another-ai-ethics-discussion)")) return "Frank Brown";
        if (url.contains("[example.com/privacy-advocacy-group](https://example.com/privacy-advocacy-group)")) return "Grace Taylor";
        if (url.contains("[example.com/economic-impact-of-automation](https://example.com/economic-impact-of-automation)")) return "Henry Wilson";
        if (url.contains("[example.com/social-commentary-politics](https://example.com/social-commentary-politics)")) return "Ivy Davis";
        if (url.contains("[example.com/environmental-policy-debate](https://example.com/environmental-policy-debate)")) return "Jack Miller";
        if (url.contains("[example.com/global-health-initiatives](https://example.com/global-health-initiatives)")) return "Karen Clark";
        if (url.contains("[example.com/education-reform-ideas](https://example.com/education-reform-ideas)")) return "Liam Hall";
        if (url.contains("[example.com/urban-development-projects](https://example.com/urban-development-projects)")) return "Mia Lewis";
        if (url.contains("[example.com/cultural-trends-analysis](https://example.com/cultural-trends-analysis)")) return "Noah Young";
        if (url.contains("[example.com/financial-market-outlook](https://example.com/financial-market-outlook)")) return "Olivia King";
        if (url.contains("[example.com/sports-analytics-insights](https://example.com/sports-analytics-insights)")) return "Peter Wright";
        if (url.contains("[example.com/scientific-discoveries](https://example.com/scientific-discoveries)")) return "Quinn Scott";
        if (url.contains("[example.com/historical-perspectives](https://example.com/historical-perspectives)")) return "Rachel Adams";
        if (url.contains("[example.com/travel-destination-guides](https://example.com/travel-destination-guides)")) return "Sam Baker";
        if (url.contains("[example.com/food-and-nutrition-tips](https://example.com/food-and-nutrition-tips)")) return "Tina Carter";
        return "Unknown Author";
    }

    /**
     * Sets the keywords for the web scanner.
     * @param newKeywords A list of new keywords.
     */
    public void setKeywords(List<String> newKeywords) {
        this.keywords = new ArrayList<>(newKeywords);
        LOGGER.info("Keywords updated to: " + newKeywords);
        // Optionally trigger an immediate scan after keywords are updated
        // performScan();
    }

    /**
     * Retrieves the latest top 20 scan results.
     * @return A list of ScanResult objects.
     */
    public List<ScanResult> getLatestScanResults() {
        // Return a copy to prevent external modification of the internal list
        synchronized (latestScanResults) {
            return new ArrayList<>(latestScanResults);
        }
    }
}
```java
// src/main/java/com/example/webapp/model/ScanResult.java
package com.example.webapp.model;

/**
 * Represents a single result from the web scan, including content,
 * sentiment analysis, political ideology ranking, summary, and author.
 */
public class ScanResult {
    private String url;
    private String content;
    private String sentiment;
    private String politicalIdeology;
    private String summary;
    private String author;

    // Default constructor for JSON deserialization (if needed)
    public ScanResult() {
    }

    public ScanResult(String url, String content, String sentiment, String politicalIdeology, String summary, String author) {
        this.url = url;
        this.content = content;
        this.sentiment = sentiment;
        this.politicalIdeology = politicalIdeology;
        this.summary = summary;
        this.author = author;
    }

    // --- Getters ---
    public String getUrl() {
        return url;
    }

    public String getContent() {
        return content;
    }

    public String getSentiment() {
        return sentiment;
    }

    public String getPoliticalIdeology() {
        return politicalIdeology;
    }

    public String getSummary() {
        return summary;
    }

    public String getAuthor() {
        return author;
    }

    // --- Setters (optional, depending on immutability requirements) ---
    public void setUrl(String url) {
        this.url = url;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public void setPoliticalIdeology(String politicalIdeology) {
        this.politicalIdeology = politicalIdeology;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "ScanResult{" +
               "url='" + url + '\'' +
               ", sentiment='" + sentiment + '\'' +
               ", politicalIdeology='" + politicalIdeology + '\'' +
               ", author='" + author + '\'' +
               '}';
    }
}
```java
// src/main/java/com/example/webapp/resource/ContentResource.java
package com.example.webapp.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.example.webapp.model.ScanResult;
import com.example.webapp.service.WebScannerService;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * JAX-RS Resource for content-related operations.
 * Exposes REST API endpoints for managing scan keywords,
 * triggering scans, and retrieving scan results.
 */
@Path("/content") // Base path for this resource
public class ContentResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentResource.class);

    @Inject // CDI will inject an instance of WebScannerService
    private WebScannerService webScannerService;

    /**
     * Endpoint to retrieve the latest top 20 scan results.
     *
     * @return A Response containing a list of ScanResult objects in JSON format.
     */
    @GET
    @Path("/results")
    @Produces(MediaType.APPLICATION_JSON) // Specifies that this method produces JSON
    public Response getScanResults() {
        LOGGER.info("Received request for scan results.");
        List<ScanResult> results = webScannerService.getLatestScanResults();
        if (results.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("No scan results available yet. Please trigger a scan.")
                           .build();
        }
        return Response.ok(results).build();
    }

    /**
     * Endpoint to trigger an immediate web scan.
     * Note: In a production environment, this might be protected or triggered by a scheduler.
     * For this example, it's exposed for demonstration.
     *
     * @return A Response indicating the scan has been initiated.
     */
    @GET
    @Path("/scan")
    @Produces(MediaType.TEXT_PLAIN)
    public Response triggerScan() {
        LOGGER.info("Received request to trigger web scan.");
        // The WebScannerService already has a scheduled task.
        // This endpoint could optionally trigger an immediate, unscheduled scan.
        // For simplicity, we'll just acknowledge the request.
        return Response.ok("Web scan initiated (or already running periodically). Check /results for updates.").build();
    }

    /**
     * Endpoint to update the keywords used by the web scanner.
     *
     * @param keywordsMap A JSON object containing a "keywords" array.
     * Example: {"keywords": ["java", "rest api", "cdi"]}
     * @return A Response indicating the keywords have been updated.
     */
    @POST
    @Path("/keywords")
    @Consumes(MediaType.APPLICATION_JSON) // Specifies that this method consumes JSON
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateKeywords(Map<String, List<String>> keywordsMap) {
        if (keywordsMap == null || !keywordsMap.containsKey("keywords")) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Invalid request body. Expected JSON with a 'keywords' array.")
                           .build();
        }
        List<String> newKeywords = keywordsMap.get("keywords");
        if (newKeywords == null || newKeywords.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Keywords list cannot be empty.")
                           .build();
        }

        webScannerService.setKeywords(newKeywords);
        LOGGER.info("Keywords updated successfully: " + newKeywords);
        return Response.ok("Keywords updated successfully.").build();
    }
}
```java
// src/main/java/com/example/webapp/filter/AuthFilter.java
package com.example.webapp.filter;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import com.example.webapp.service.AuthService;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * JAX-RS ContainerRequestFilter for authentication.
 * This filter intercepts incoming requests and checks for a valid
 * Authorization header (e.g., Bearer token).
 *
 * All requests to JAX-RS resources will pass through this filter.
 */
@Provider // Makes this class a JAX-RS provider, so it's automatically discovered
public class AuthFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);

    @Inject // CDI will inject an instance of AuthService
    private AuthService authService;

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info("AuthFilter intercepting request to: " + requestContext.getUriInfo().getPath());

        // Allow access to the OAuth callback endpoint without authentication
        if (requestContext.getUriInfo().getPath().equals("auth/callback") ||
            requestContext.getUriInfo().getPath().equals("auth/initiate")) { // Assuming an initiate endpoint
            LOGGER.info("Skipping authentication for OAuth callback/initiate.");
            return;
        }

        // Get the Authorization header from the request
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Check if the Authorization header is present and correctly formatted
        if (authorizationHeader == null || !authorizationHeader.startsWith(AUTHENTICATION_SCHEME + " ")) {
            LOGGER.warn("Missing or invalid Authorization header.");
            abortWithUnauthorized(requestContext, "Authorization header must be provided in 'Bearer [token]' format.");
            return;
        }

        // Extract the token from the Authorization header
        String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

        try {
            // Validate the token using the AuthService
            if (!authService.validateAccessToken(token)) {
                LOGGER.warn("Invalid or expired access token.");
                abortWithUnauthorized(requestContext, "Invalid or expired access token.");
                return;
            }
            LOGGER.info("Access token validated successfully.");
            // If the token is valid, the request can proceed
        } catch (Exception e) {
            LOGGER.error("Error during token validation: {}", e.getMessage());
            abortWithUnauthorized(requestContext, "Authentication failed due to server error.");
        }
    }

    /**
     * Aborts the request with a 401 Unauthorized response.
     * @param requestContext The request context.
     * @param message The message to include in the response.
     */
    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, AUTHENTICATION_SCHEME)
                        .entity(message)
                        .build()
        );
    }
}
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="[https://jakarta.ee/xml/ns/jakartaee](https://jakarta.ee/xml/ns/jakartaee)"
       xmlns:xsi="[http://www.w3.org/2001/XMLSchema-instance](http://www.w3.org/2001/XMLSchema-instance)"
       xsi:schemaLocation="[https://jakarta.ee/xml/ns/jakartaee](https://jakarta.ee/xml/ns/jakartaee)
                           [https://jakarta.ee/xml/ns/jakartaee/beans_3_0.xsd](https://jakarta.ee/xml/ns/jakartaee/beans_3_0.xsd)"
       version="3.0" bean-discovery-mode="all">
    <!--
        This empty beans.xml file is required by CDI (Weld) to enable
        bean discovery in the application.
        The 'bean-discovery-mode="all"' attribute ensures that all
        classes with CDI annotations (like @ApplicationScoped, @Inject)
        are automatically discovered and managed by the CDI container.
    -->
</beans>
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example.webapp</groupId>
    <artifactId>rest-api-java-app</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <jetty.version>11.0.18</jetty.version> <jersey.version>3.1.5</jersey.version> <weld.version>5.1.1.Final</weld.version> <jakarta.servlet.version>6.0.0</jakarta.servlet.version>
        <jakarta.ws.rs-api.version>3.1.0</jakarta.ws.rs-api.version>
        <jakarta.inject-api.version>2.0.1</jakarta.inject-api.version>
        <freemarker.version>2.3.32</freemarker.version>
        <jsoup.version>1.17.2</jsoup.version>
        <jackson.version>2.17.1</jackson.version> </properties>

    <dependencies>
        <dependency>
            <groupId>org.eclipse.jetty</groupId>
            <artifactId>jetty-server</artifactId>
            <version>${jetty.version}</version>
        </dependency>
        <dependency>
            <groupId>org.eclipse.jetty</groupId>
            <artifactId>jetty-servlet</artifactId>
            <version>${jetty.version}</version>
        </dependency>
        <dependency>
            <groupId>org.eclipse.jetty</groupId>
            <artifactId>jetty-webapp</artifactId>
            <version>${jetty.version}</version>
        </dependency>

        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>${jakarta.servlet.version}</version>
            <scope>provided</scope> </dependency>

        <dependency>
            <groupId>org.glassfish.jersey.containers</groupId>
            <artifactId>jersey-container-servlet</artifactId>
            <version>${jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.inject</groupId>
            <artifactId>jersey-hk2</artifactId> <version>${jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.media</groupId>
            <artifactId>jersey-media-json-jackson</artifactId> <version>${jersey.version}</version>
        </dependency>

        <dependency>
            <groupId>jakarta.ws.rs</groupId>
            <artifactId>jakarta.ws.rs-api</artifactId>
            <version>${jakarta.ws.rs-api.version}</version>
        </dependency>
        <dependency>
            <groupId>jakarta.enterprise</groupId>
            <artifactId>jakarta.enterprise.cdi-api</artifactId>
            <version>${jakarta.inject-api.version}</version>
        </dependency>
        <dependency>
            <groupId>jakarta.inject</groupId>
            <artifactId>jakarta.inject-api</artifactId>
            <version>${jakarta.inject-api.version}</version>
        </dependency>

        <dependency>
            <groupId>org.jboss.weld.servlet</groupId>
            <artifactId>weld-servlet-jakarta</artifactId>
            <version>${weld.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jboss.weld.module</groupId>
            <artifactId>weld-jsf</artifactId> <version>${weld.version}</version>
        </dependency>

        <dependency>
            <groupId>org.freemarker</groupId>
            <artifactId>freemarker</artifactId>
            <version>${freemarker.version}</version>
        </dependency>

        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>${jsoup.version}</version>
        </dependency>

        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>${jackson.version}</version>
        </dependency>

        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.13</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.13</version>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.11.0-M1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>5.11.0-M1</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <source>${maven.compiler.source}</source>
                    <target>${maven.compiler.target}</target>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.6.0</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>com.example.webapp.App</mainClass>
                                </transformer>
                            </transformers>
                            <filters>
                                <filter>
                                    <artifact>*:*</artifact>
                                    <excludes>
                                        <exclude>META-INF/*.SF</exclude>
                                        <exclude>META-INF/*.DSA</exclude>
                                        <exclude>META-INF/*.RSA</exclude>
                                    </excludes>
                                </filter>
                            </filters>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
