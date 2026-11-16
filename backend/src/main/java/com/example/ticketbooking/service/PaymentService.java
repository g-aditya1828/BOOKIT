package com.example.ticketbooking.service;

import com.example.ticketbooking.config.RazorpayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Handles Razorpay payment gateway integration.
 * Creates orders and verifies HMAC payment signatures.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final String RAZORPAY_ORDERS_URL = "https://api.razorpay.com/v1/orders";

    private final RazorpayConfig razorpayConfig;

    /**
     * Create a Razorpay order by calling the Razorpay API.
     * Amount is converted from rupees to paise (multiply by 100).
     * Uses Basic authentication with keyId:keySecret.
     *
     * @return Razorpay order ID string
     */
    public String createRazorpayOrder(BigDecimal amount, String receipt) {
        try {
            // Convert amount to paise (Razorpay expects smallest currency unit)
            long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();

            String requestBody = String.format(
                    "{\"amount\":%d,\"currency\":\"INR\",\"receipt\":\"%s\"}",
                    amountInPaise, receipt);

            // Build Basic auth header: Base64(keyId:keySecret)
            String credentials = razorpayConfig.getKeyId() + ":" + razorpayConfig.getKeySecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(
                    credentials.getBytes(StandardCharsets.UTF_8));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RAZORPAY_ORDERS_URL))
                    .header("Authorization", "Basic " + encodedCredentials)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                // Parse order ID from JSON response (simple string extraction)
                String body = response.body();
                String orderId = extractJsonValue(body, "id");
                log.info("Created Razorpay order: {}", orderId);
                return orderId;
            } else {
                log.error("Razorpay order creation failed: {} - {}", response.statusCode(), response.body());
                // Return a mock order ID for development when Razorpay is not configured
                return "order_" + System.currentTimeMillis();
            }
        } catch (IOException | InterruptedException e) {
            log.warn("Razorpay API call failed (using mock order ID): {}", e.getMessage());
            // Fallback mock order ID for development without Razorpay configured
            return "order_" + System.currentTimeMillis();
        }
    }

    /**
     * Verify the Razorpay payment signature using HMAC-SHA256.
     * The expected signature is HMAC-SHA256(orderId + "|" + paymentId, keySecret).
     *
     * @return true if the signature matches, false otherwise
     */
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    razorpayConfig.getKeySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedSignature = HexFormat.of().formatHex(hash);
            return computedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    /** Simple JSON value extractor for a given key (avoids adding Jackson dependency on service layer). */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) return null;
        start += searchKey.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }
}
