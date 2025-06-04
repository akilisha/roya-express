package com.akilisha.oss.payman.handler;

import com.akilisha.oss.payman.sse.SsePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

// This class needs to be listed in META-INF/services/com.akilisha.oss.payman.handler.BaseHandler
public class SseHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SseHandler.class);
    private SsePublisher ssePublisher; // Injected SSE publisher

    public void setSsePublisher(SsePublisher ssePublisher) {
        this.ssePublisher = ssePublisher;
    }

    @Override
    public String getPath() {
        return "/sse";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/event-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Connection", "keep-alive");

        String clientId = req.getSession().getId(); // Use session ID to uniquely identify client

        if (clientId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("data: Unauthorized client. Session ID missing.\n\n");
            return;
        }

        LOGGER.info("SSE client connected: {}", clientId);
        PrintWriter writer = resp.getWriter();

        // Register client with the SSE publisher
        ssePublisher.addClient(clientId, writer);

        // Keep the connection open
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Keep the connection alive. Send a comment event periodically.
                writer.write(":keep-alive\n\n");
                writer.flush();
                Thread.sleep(30000); // Send keep-alive every 30 seconds
            }
        } catch (InterruptedException e) {
            LOGGER.info("SSE connection for client {} interrupted.", clientId);
            Thread.currentThread().interrupt(); // Restore interrupt status
        } finally {
            ssePublisher.removeClient(clientId);
            LOGGER.info("SSE client disconnected: {}", clientId);
        }
    }
}
