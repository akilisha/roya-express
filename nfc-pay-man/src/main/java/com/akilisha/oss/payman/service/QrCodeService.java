package com.akilisha.oss.payman.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class QrCodeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QrCodeService.class);

    public String generateQrCodeData(String amount, String description, String paymentOption) {
        // In a real application, you would use a QR code library (e.g., ZXing)
        // to generate an actual QR code image (e.g., as a Base64 encoded PNG).
        // The data encoded in the QR code could be a URL, a JSON string, or a payment request URI.

        // For this simulation, we'll just encode a simple string representing the payment details.
        String dataToEncode = String.format("PAYMENT_REQUEST: Amount=%s, Desc=%s, Option=%s", amount, description, paymentOption);
        String base64QrCodeImage = Base64.getEncoder().encodeToString(dataToEncode.getBytes());

        LOGGER.info("QrCodeService: Generated mock QR code data for: {}", dataToEncode);
        return "data:image/png;base64," + base64QrCodeImage; // Simulate a data URI for an image
    }
}
