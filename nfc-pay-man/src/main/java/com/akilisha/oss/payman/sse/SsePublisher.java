package com.akilisha.oss.payman.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SsePublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SsePublisher.class);
    private final Map<String, PrintWriter> clients = new ConcurrentHashMap<>();

    public void addClient(String clientId, PrintWriter writer) {
        clients.put(clientId, writer);
        LOGGER.info("SSEPublisher: Client added - {}", clientId);
    }

    public void removeClient(String clientId) {
        PrintWriter client = clients.remove(clientId);
        LOGGER.info("SSEPublisher: Client removed - {}", clientId);
        client.close();
    }

    public void publish(String clientId, String eventType, String data) {
        PrintWriter writer = clients.get(clientId);
        if (writer != null) {
            try {
                writer.write("event: " + eventType + "\n");
                writer.write("data: " + data + "\n\n");
                writer.flush();
                LOGGER.info("SSEPublisher: Published event '{}' to client {}", eventType, clientId);
            } catch (Exception e) {
                LOGGER.warn("SSEPublisher: Failed to publish to client {}: {}", clientId, e.getMessage());
                removeClient(clientId); // Remove client if write fails
            }
        } else {
            LOGGER.warn("SSEPublisher: Client {} not found for publishing.", clientId);
        }
    }

    public void publishToAll(String eventType, String data) {
        clients.forEach((clientId, writer) -> publish(clientId, eventType, data));
    }
}
