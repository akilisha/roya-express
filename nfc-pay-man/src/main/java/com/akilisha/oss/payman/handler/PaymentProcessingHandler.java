package com.akilisha.oss.payman.handler;

import com.akilisha.oss.payman.service.PaymentService;
import com.akilisha.oss.payman.sse.SsePublisher;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PaymentProcessingHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessingHandler.class);
    private final Gson gson = new Gson();
    private PaymentService paymentService; // Injected dependency
    private SsePublisher ssePublisher; // Injected SSE publisher

    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void setSsePublisher(SsePublisher ssePublisher) {
        this.ssePublisher = ssePublisher;
    }

    @Override
    public String getPath() {
        return "/process-payment";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String payload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        try {
            Map<String, Object> paymentDetails = gson.fromJson(payload, Map.class);
            String transactionId = "txn_" + UUID.randomUUID().toString().substring(0, 8);
            String userId = (String) req.getSession().getAttribute("userId"); // Get user from session

            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.setContentType("application/json");
                resp.getWriter().write(gson.toJson(new PaymentResponse(false, "Unauthorized: User not logged in.", null)));
                return;
            }

            // In a real scenario, paymentDetails would contain a token from the client-side SDK (Adyen/Stripe)
            // and other relevant transaction info.
            // The paymentService would then call the PSP's API.
            boolean success = paymentService.processPayment(transactionId, userId, paymentDetails);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(new PaymentResponse(success, success ? "Payment processed successfully." : "Payment failed.", transactionId)));

            // Push targeted update to the client via SSE
            if (ssePublisher != null) {
                String clientId = req.getSession().getId(); // Use session ID as client ID for SSE
                ssePublisher.publish(clientId, "payment_status", gson.toJson(new PaymentStatusUpdate(transactionId, success ? "COMPLETED" : "FAILED", userId)));
                LOGGER.info("Published SSE payment status for client: {}, transaction: {}", clientId, transactionId);
            }

        } catch (Exception e) {
            LOGGER.error("Error processing payment: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(new PaymentResponse(false, "Internal server error: " + e.getMessage(), null)));
        }
    }

    private static class PaymentResponse {
        boolean success;
        String message;
        String transactionId;

        public PaymentResponse(boolean success, String message, String transactionId) {
            this.success = success;
            this.message = message;
            this.transactionId = transactionId;
        }
    }

    private static class PaymentStatusUpdate {
        String transactionId;
        String status;
        String userId;

        public PaymentStatusUpdate(String transactionId, String status, String userId) {
            this.transactionId = transactionId;
            this.status = status;
            this.userId = userId;
        }
    }
}
