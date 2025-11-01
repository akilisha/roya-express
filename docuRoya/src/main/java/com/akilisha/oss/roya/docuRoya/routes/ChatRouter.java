package com.akilisha.oss.roya.docuRoya.routes;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.plugins.auth.Auth;
import com.akilisha.oss.roya.plugins.auth.User;
import io.helidon.websocket.WsListener;
import io.helidon.websocket.WsSession;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Chat routes with SSE and WebSocket support.
 *
 * Demonstrates:
 * - Server-Sent Events (SSE) for real-time updates
 * - WebSocket chat endpoint
 * - Session-backed chat service
 * - Message broadcasting
 */
public class ChatRouter {
    private final Roya app;
    private final Auth auth;

    // Session-backed chat service
    private static class ChatSession {
        final String id;
        final User user;
        final Instant createdAt;
        int messageCount;

        ChatSession(String id, User user) {
            this.id = id;
            this.user = user;
            this.createdAt = Instant.now();
            this.messageCount = 0;
        }
    }

    // Global chat state
    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final AtomicInteger totalMessages = new AtomicInteger(0);
    private final Map<String, WsSession> wsConnections = new ConcurrentHashMap<>();

    public ChatRouter(Roya app) {
        this.app = app;
        this.auth = app.services().get(Auth.class);
    }

    public void register() {
        // Send message to chat (simulates user action)
        app.post("/api/chat/message", auth.required(), (Request req, Response res, Next next) -> {
            User user = (User) req.get("user");
            Map<String, Object> body = req.body(Map.class);
            String sessionId = (String) body.getOrDefault("session", "default");
            String message = (String) body.get("message");

            if (message == null) {
                res.status(400).json(Map.of("error", "message required"));
                return;
            }

            // Update session message count
            ChatSession session = sessions.computeIfAbsent(sessionId, id -> new ChatSession(id, user));
            session.messageCount++;
            totalMessages.incrementAndGet();

            // Broadcast to WebSocket clients
            String broadcastMsg = String.format("{\"type\":\"message\",\"session\":\"%s\",\"user\":\"%s\",\"message\":\"%s\",\"count\":%d}",
                    sessionId, user.email(), message, session.messageCount);
            for (var wsSession : wsConnections.values()) {
                try {
                    wsSession.send(broadcastMsg, false);
                } catch (Exception e) {
                    // Connection closed, ignore
                }
            }

            res.json(Map.of(
                    "session", sessionId,
                    "messageCount", session.messageCount,
                    "totalMessages", totalMessages.get(),
                    "timestamp", Instant.now().toString()
            ));
        });

        // Get chat stats
        app.get("/api/chat/stats", auth.required(), (Request req, Response res, Next next) -> {
            res.json(Map.of(
                    "activeSessions", sessions.size(),
                    "wsConnections", wsConnections.size(),
                    "totalMessages", totalMessages.get(),
                    "sessions", sessions.values().stream().map(s -> Map.of(
                            "id", s.id,
                            "user", s.user.email(),
                            "messageCount", s.messageCount,
                            "createdAt", s.createdAt.toString()
                    )).toList()
            ));
        });

        // WebSocket chat endpoint
        app.ws("/ws/chat", new ChatWebSocketListener());
    }

    /**
     * WebSocket listener for chat.
     * Handles connections, disconnections, and message broadcasting.
     */
    private class ChatWebSocketListener implements WsListener {
        @Override
        public void onOpen(WsSession session) {
            String connId = String.valueOf(session.hashCode()); // Use hashCode as ID
            wsConnections.put(connId, session);
            System.out.println("WebSocket connected: " + connId);

            // Send welcome message
            try {
                session.send("{\"type\":\"connected\",\"message\":\"Welcome to DocuRoya Chat!\"}", false);
            } catch (Exception e) {
                System.err.println("Failed to send welcome message: " + e.getMessage());
            }
        }

        @Override
        public void onClose(WsSession session, int status, String reason) {
            String connId = String.valueOf(session.hashCode());
            wsConnections.remove(connId);
            System.out.println("WebSocket disconnected: " + connId + " (" + reason + ")");
        }

        @Override
        public void onMessage(WsSession session, String text, boolean last) {
            try {
                // Echo the message to all connected clients
                String connId = String.valueOf(session.hashCode());
                String echoMsg = String.format("{\"type\":\"echo\",\"message\":\"%s\",\"from\":\"%s\"}",
                        text, connId);

                for (var wsSession : wsConnections.values()) {
                    wsSession.send(echoMsg, false);
                }
            } catch (Exception e) {
                System.err.println("Failed to broadcast message: " + e.getMessage());
            }
        }

        @Override
        public void onError(WsSession session, Throwable throwable) {
            String connId = String.valueOf(session.hashCode());
            System.err.println("WebSocket error: " + throwable.getMessage());
            wsConnections.remove(connId);
        }
    }
}

