package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Handler;
import io.helidon.websocket.WsListener;
import io.helidon.websocket.WsSession;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WebSocketDemo {
    public static void main(String[] args) {
        var app = Roya.create();

        // Health
        app.get("/", (req, res, next) -> res.json(Map.of("status","ok")));

        // Simple echo WebSocket at /ws/echo
        WsListener echo = new WsListener() {
            @Override
            public void onOpen(WsSession session) {
                session.send("connected", true);
            }

            @Override
            public void onMessage(WsSession session, String text, boolean last) {
                session.send("echo: " + text, true);
            }
        };

        app.ws("/ws/echo", echo);

        app.listen(3004, () -> System.out.println("WebSocket demo on ws://localhost:3004/ws/echo"));
    }
}


