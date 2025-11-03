package com.akilisha.oss.roya.plugins.ai;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AIServiceNewApiTest {

    static class FakeProvider implements com.akilisha.oss.roya.plugins.ai.providers.LLMProvider {
        @Override
        public String name() { return "fake"; }
        @Override
        public LLMResponse complete(String system, String user, AIOptions options) {
            return new LLMResponse("ok:" + user, "model-x", 10, 20, 30, java.util.Optional.of("stop"));
        }

        @Override
        public void stream(String system, String user, AIOptions options, Consumer<String> onToken) {
            onToken.accept("tok1");
            onToken.accept("tok2");
        }

        @Override
        public LLMResponse completeJson(String system, String user, AIOptions options) {
            // Return simple JSON that matches TestRecord
            return new LLMResponse("{\"name\":\"Widget\",\"price\":9.99}", "model-x", 10, 20, 30, java.util.Optional.of("stop"));
        }
    }

    public record TestRecord(String name, Double price) {}

    @Test
    void llmAsk_extract_stream_and_ragFallback_work() {
        var ai = new AIServiceImpl(new FakeProvider(), null);

        // ask
        String answer = ai.llm().ask("sys", "hello");
        assertEquals("ok:hello", answer);

        // extract
        TestRecord rec = ai.llm().extract(TestRecord.class, "text");
        assertEquals("Widget", rec.name());
        assertEquals(9.99, rec.price());

        // stream
        AtomicInteger tokens = new AtomicInteger();
        ai.llm().stream("sys", "user", s -> tokens.incrementAndGet());
        assertEquals(2, tokens.get());

        // rag fallback (no OPENAI_API_KEY set -> fallback path)
        var rag = ai.rag("What is X?");
        assertNotNull(rag);
        assertNotNull(rag.answer());
        assertTrue(rag.answer().startsWith("ok:"));
    }
}


