package com.akilisha.oss.roya.examples.mcp;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.core.middleware.Cors;
import com.akilisha.oss.roya.core.middleware.Morgan;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.RAGOptions;
import com.akilisha.oss.roya.plugins.ai.RAGResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class DocsMcpServer {

    public record DocQuery(String query, Integer topK) {}
    public record Citation(String source, Integer chunkIndex, Double score) {}
    public record DocAnswer(String summary, List<Citation> citations) {}

    public static void main(String[] args) {
        var app = Roya.create();
        var objectMapper = new ObjectMapper();

        app.use(Morgan.combined());
        app.use(Cors.cors());
        app.use(BodyParser.bodyParser());

        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        try { aiPlugin.start(); } catch (Exception ignored) {}

        app.post("/mcp/tools/docs.searchAndSummarize", (Request req, Response res, Next next) -> {
            try {
                Map<String, Object> body = readJsonBody(req, objectMapper);
                if (body == null || !body.containsKey("query")) {
                    res.status(400).json(Map.of("error", "missing 'query'"));
                    return;
                }
                String query = String.valueOf(body.get("query"));
                int topK = body.get("topK") instanceof Number n ? n.intValue() : 5;

                AI ai = req.get(AI.class);
                RAGOptions opts = RAGOptions.builder().topK(topK).build();
                RAGResponse rag = ai.ragApi().ask(query, opts);

                List<Citation> citations = rag.sources().stream().map(d -> {
                    String source = String.valueOf(d.metadata().getOrDefault("source", d.id()));
                    Integer idx = (Integer) d.metadata().getOrDefault("chunkIndex", 0);
                    return new Citation(source, idx, 0.0);
                }).toList();

                res.json(new DocAnswer(rag.answer(), citations));
            } catch (Exception e) {
                res.status(500).json(Map.of("error", e.getMessage()));
            }
        });

        int port = Integer.parseInt(System.getProperty("mcp.port", "3002"));
        app.listen(port, () -> System.out.println("Docs MCP demo running on http://localhost:" + port));
    }

    private static Map<String, Object> readJsonBody(Request req, ObjectMapper objectMapper) {
        try {
            String text = req.bodyText();
            if (text != null && !text.isBlank()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = objectMapper.readValue(text, Map.class);
                return map;
            }
        } catch (Exception e) {
            System.err.println("Error reading JSON body: " + e.getMessage());
        }
        return null;
    }
}


