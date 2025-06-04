package com.akilisha.oss.payman.handler;

import com.akilisha.oss.payman.service.PaymentService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

// This class needs to be listed in META-INF/services/com.akilisha.oss.payman.handler.BaseHandler
public class PaymentMethodHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentMethodHandler.class);
    private final Gson gson = new Gson();
    private PaymentService paymentService; // Injected dependency

    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public String getPath() {
        return "/payment-methods";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Simulate requesting payment methods from a PSP (Adyen/Stripe)
        // In a real scenario, this would involve calling the PSP's API.
        // The paymentService would encapsulate this call.

        List<String> availableMethods = paymentService.getAvailablePaymentMethods();

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(availableMethods));
        LOGGER.info("Requested available payment methods.");
    }
}
