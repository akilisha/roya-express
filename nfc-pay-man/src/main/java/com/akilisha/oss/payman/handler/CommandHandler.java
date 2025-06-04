package com.akilisha.oss.payman.handler;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

// This class needs to be listed in META-INF/services/com.akilisha.oss.payman.handler.BaseHandler
public class CommandHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);
    private final Gson gson = new Gson();

    @Override
    public String getPath() {
        return "/command";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String payload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        try {
            Map<String, Object> command = gson.fromJson(payload, Map.class);
            LOGGER.info("Received command: {}", command);

            // In a real application, you would parse the 'command' JSON
            // and dispatch it to appropriate services based on a 'type' or 'action' field.
            // Example:
            // String commandType = (String) command.get("type");
            // switch (commandType) {
            //     case "updateSettings": // call settingsService.update(...)
            //     case "processRefund": // call paymentService.refund(...)
            //     default: // handle unknown command
            // }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(new CommandResponse(true, "Command received and processed (simulated).")));
        } catch (Exception e) {
            LOGGER.error("Error processing command: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(new CommandResponse(false, "Invalid command format or processing error: " + e.getMessage())));
        }
    }

    private static class CommandResponse {
        boolean success;
        String message;

        public CommandResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
