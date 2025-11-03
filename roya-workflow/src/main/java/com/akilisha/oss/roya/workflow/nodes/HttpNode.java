package com.akilisha.oss.roya.workflow.nodes;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP request node for making API calls
 */
public class HttpNode implements WorkflowNode {

    private final HttpClient client;
    private final String url;
    private final String method;
    private final Map<String, String> headers;

    public HttpNode(String url, String method) {
        this(url, method, Map.of());
    }

    public HttpNode(String url, String method, Map<String, String> headers) {
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.url = url;
        this.method = method.toUpperCase();
        this.headers = headers;
    }

    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        try {
            // Build request
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30));

            // Add headers
            headers.forEach(requestBuilder::header);

            // Add body if present
            String body = input.getString("body");
            if (body != null && !method.equals("GET")) {
                requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(body));
            } else {
                requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            HttpRequest request = requestBuilder.build();

            // Send async
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> NodeOutput.success(Map.of(
                    "statusCode", response.statusCode(),
                    "body", response.body(),
                    "headers", response.headers().map()
                )))
                .exceptionally(ex -> NodeOutput.failure("HTTP request failed: " + ex.getMessage()));

        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                NodeOutput.failure("Failed to build HTTP request: " + e.getMessage())
            );
        }
    }
}
