package com.akilisha.oss.payman.handler;

import com.akilisha.oss.payman.service.QrCodeService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// This class needs to be listed in META-INF/services/com.akilisha.oss.payman.handler.BaseHandler
public class QrCodeHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(QrCodeHandler.class);
    private final Gson gson = new Gson();
    private QrCodeService qrCodeService; // Injected dependency

    public void setQrCodeService(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @Override
    public String getPath() {
        return "/qr-code";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String amount = req.getParameter("amount");
        String description = req.getParameter("description");
        String paymentOption = req.getParameter("paymentOption"); // e.g., "card", "wallet"

        if (amount == null || description == null || paymentOption == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(new StatusResponse(false, "Missing amount, description, or paymentOption.")));
            return;
        }

        // Simulate QR code generation with encoded payment details
        String qrCodeData = qrCodeService.generateQrCodeData(amount, description, paymentOption);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(new QrCodeResponse(true, "QR Code data generated.", qrCodeData)));
        LOGGER.info("Generated QR code for amount: {}, description: {}" ,amount, description);
    }

    private static class QrCodeResponse {
        boolean success;
        String message;
        String qrCodeData; // Base64 encoded image or raw data string

        public QrCodeResponse(boolean success, String message, String qrCodeData) {
            this.success = success;
            this.message = message;
            this.qrCodeData = qrCodeData;
        }
    }

    private static class StatusResponse {
        boolean success;
        String message;

        public StatusResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
