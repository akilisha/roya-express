package com.akilisha.oss.roya.plugins.ai.webhooks;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

/**
 * Generic webhook signature verification middleware.
 * 
 * Supports multiple signature algorithms and header formats:
 * - HMAC-SHA256 (GitHub, Stripe, etc.)
 * - HMAC-SHA1 (legacy)
 * - Custom header names
 * 
 * Usage:
 * <pre>
 * // As middleware
 * app.post("/webhook", WebhookSignatureVerifier.middleware("secret", "sha256"), handler);
 * 
 * // Inline verification
 * if (WebhookSignatureVerifier.verify(request, "secret", "sha256")) {
 *     // Process webhook
 * }
 * </pre>
 */
public class WebhookSignatureVerifier {
    
    /**
     * Signature algorithm types.
     */
    public enum Algorithm {
        SHA256("HmacSHA256", "sha256", "sha256="),
        SHA1("HmacSHA1", "sha1", "sha1=");
        
        private final String jcaName;
        private final String name;
        private final String prefix;
        
        Algorithm(String jcaName, String name, String prefix) {
            this.jcaName = jcaName;
            this.name = name;
            this.prefix = prefix;
        }
        
        public String getJcaName() {
            return jcaName;
        }
        
        public String getName() {
            return name;
        }
        
        public String getPrefix() {
            return prefix;
        }
        
        public static Algorithm fromString(String name) {
            for (Algorithm alg : values()) {
                if (alg.name.equalsIgnoreCase(name)) {
                    return alg;
                }
            }
            return SHA256; // Default
        }
    }
    
    /**
     * Common signature header names used by different providers.
     */
    public enum HeaderName {
        X_HUB_SIGNATURE_256("X-Hub-Signature-256"),  // GitHub
        X_HUB_SIGNATURE("X-Hub-Signature"),          // GitHub (legacy)
        X_SIGNATURE("X-Signature"),                  // Generic
        STRIPE_SIGNATURE("Stripe-Signature"),        // Stripe
        X_WEBHOOK_SIGNATURE("X-Webhook-Signature");  // Generic
        
        private final String headerName;
        
        HeaderName(String headerName) {
            this.headerName = headerName;
        }
        
        public String getHeaderName() {
            return headerName;
        }
    }
    
    /**
     * Verify webhook signature.
     * 
     * @param request HTTP request
     * @param secret Secret key
     * @param algorithm Signature algorithm
     * @param headerName Header name containing signature
     * @return true if signature is valid
     */
    public static boolean verify(Request request, String secret, Algorithm algorithm, String headerName) {
        if (secret == null || secret.isEmpty()) {
            return false; // No secret configured, verification disabled
        }
        
        // Get signature from header
        Optional<String> signatureHeader = request.headers().get(headerName);
        if (signatureHeader.isEmpty()) {
            return false;
        }
        
        String signature = signatureHeader.get();
        
        // Get request body
        String body = request.bodyText();
        if (body == null || body.isEmpty()) {
            return false;
        }
        
        // Calculate expected signature
        String expectedSignature = calculateSignature(body, secret, algorithm);
        
        // Compare signatures (constant-time comparison to prevent timing attacks)
        return constantTimeEquals(signature, expectedSignature);
    }
    
    /**
     * Verify webhook signature using default header (X-Hub-Signature-256).
     */
    public static boolean verify(Request request, String secret, Algorithm algorithm) {
        return verify(request, secret, algorithm, HeaderName.X_HUB_SIGNATURE_256.getHeaderName());
    }
    
    /**
     * Verify webhook signature using default algorithm (SHA256).
     */
    public static boolean verify(Request request, String secret) {
        return verify(request, secret, Algorithm.SHA256);
    }
    
    /**
     * Verify signature with format: "sha256=abc123..." (GitHub format).
     */
    public static boolean verifyWithPrefix(Request request, String secret, Algorithm algorithm, String headerName) {
        if (secret == null || secret.isEmpty()) {
            return false;
        }
        
        Optional<String> signatureHeader = request.headers().get(headerName);
        if (signatureHeader.isEmpty()) {
            return false;
        }
        
        String signature = signatureHeader.get();
        
        // Strip algorithm prefix if present (e.g., "sha256=abc123" -> "abc123")
        String prefix = algorithm.getPrefix();
        if (signature.startsWith(prefix)) {
            signature = signature.substring(prefix.length());
        }
        
        String body = request.bodyText();
        if (body == null || body.isEmpty()) {
            return false;
        }
        
        String expectedSignature = calculateSignature(body, secret, algorithm);
        
        return constantTimeEquals(signature, expectedSignature);
    }
    
    /**
     * Calculate HMAC signature.
     */
    private static String calculateSignature(String payload, String secret, Algorithm algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm.getJcaName());
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                algorithm.getJcaName()
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to calculate signature", e);
        }
    }
    
    /**
     * Constant-time string comparison to prevent timing attacks.
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
    
    /**
     * Create middleware for signature verification.
     * 
     * @param secret Secret key (can be null/empty to disable verification)
     * @param algorithm Signature algorithm
     * @param headerName Header name containing signature
     * @return Middleware handler
     */
    public static Handler middleware(String secret, Algorithm algorithm, String headerName) {
        return (Request req, Response res, Next next) -> {
            if (secret != null && !secret.isEmpty()) {
                if (!verify(req, secret, algorithm, headerName)) {
                    res.status(401).json(java.util.Map.of(
                        "error", "Invalid signature",
                        "message", "Webhook signature verification failed"
                    ));
                    return;
                }
            }
            next.handle(req, res);
        };
    }
    
    /**
     * Create middleware with default header.
     */
    public static Handler middleware(String secret, Algorithm algorithm) {
        return middleware(secret, algorithm, HeaderName.X_HUB_SIGNATURE_256.getHeaderName());
    }
    
    /**
     * Create middleware with default algorithm and header.
     */
    public static Handler middleware(String secret) {
        return middleware(secret, Algorithm.SHA256);
    }
}

