package com.akilisha.oss.payman.handler;

import com.akilisha.oss.payman.model.Device;
import com.akilisha.oss.payman.service.DeviceService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

// This class needs to be listed in META-INF/services/com.akilisha.oss.payman.handler.BaseHandler
public class OAuthHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthHandler.class);
    private final Gson gson = new Gson();
    private DeviceService deviceService; // Injected dependency

    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public String getPath() {
        return "/auth/oauth";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Simulate OAuth callback
        String code = req.getParameter("code");
        String state = req.getParameter("state"); // Should be validated against session state

        if (code != null) {
            // In a real scenario:
            // 1. Exchange 'code' for access_token and ID token with OAuth provider (backend call).
            // 2. Validate ID token.
            // 3. Get user info from ID token or userinfo endpoint.
            // 4. Create/retrieve user session.
            // 5. Potentially register/update device associated with user.

            String mockUserId = "oauth_user_" + UUID.randomUUID().toString().substring(0, 8);
            String mockDeviceId = req.getSession().getId(); // Use session ID as mock device ID for demo

            // Simulate device registration/update
            Device device = new Device(mockDeviceId, mockUserId, "OAuth Device", System.currentTimeMillis());
            deviceService.registerDevice(device);
            LOGGER.info("Simulated OAuth login for user: {} and device: {}", mockUserId, mockDeviceId);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(new AuthResponse(true, "OAuth login simulated successfully for user " + mockUserId)));
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(new AuthResponse(false, "OAuth code missing.")));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Simulate initiating OAuth flow (e.g., redirect to Google/Auth0 login page)
        // In a real app, you'd send redirect headers here.
        String provider = req.getParameter("provider");
        if (provider != null && !provider.isEmpty()) {
            String redirectUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=YOUR_GOOGLE_CLIENT_ID&redirect_uri=http://localhost:8080/auth/oauth&response_type=code&scope=openid%20profile%20email&state=" + UUID.randomUUID().toString();
            if ("Auth0".equalsIgnoreCase(provider)) {
                redirectUrl = "https://YOUR_AUTH0_DOMAIN/authorize?audience=YOUR_API_AUDIENCE&scope=openid%20profile%20email&response_type=code&client_id=YOUR_AUTH0_CLIENT_ID&redirect_uri=http://localhost:8080/auth/oauth";
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(new AuthResponse(true, "Redirecting to " + provider + " for authentication.", redirectUrl)));
            LOGGER.info("Simulated OAuth initiation for provider: {}", provider);
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(new AuthResponse(false, "OAuth provider not specified.")));
        }
    }

    private static class AuthResponse {
        boolean success;
        String message;
        String redirectUrl;

        public AuthResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public AuthResponse(boolean success, String message, String redirectUrl) {
            this.success = success;
            this.message = message;
            this.redirectUrl = redirectUrl;
        }
    }
}
