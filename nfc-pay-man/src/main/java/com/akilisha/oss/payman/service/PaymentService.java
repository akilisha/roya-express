package com.akilisha.oss.payman.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PaymentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    public List<String> getAvailablePaymentMethods() {
        // Simulate calling Adyen/Stripe API to get available payment methods
        LOGGER.info("PaymentService: Fetching available payment methods from PSP.");
        return Arrays.asList("Visa", "MasterCard", "Amex", "PayPal", "Apple Pay (Simulated)");
    }

    public boolean processPayment(String transactionId, String userId, Map<String, Object> paymentDetails) {
        // Simulate calling Adyen/Stripe API to process payment
        // paymentDetails would contain the tokenized card data or other payment method details.
        LOGGER.info("PaymentService: Processing payment transaction {}", transactionId + " for user " + userId + " with details: " + paymentDetails);

        // Simulate success/failure based on some condition or randomness
        boolean success = Math.random() > 0.1; // 90% chance of success for demo

        if (success) {
            LOGGER.info("PaymentService: Transaction {} successful.", transactionId);
        } else {
            LOGGER.warn("PaymentService: Transaction {} failed.", transactionId);
        }
        return success;
    }
}
