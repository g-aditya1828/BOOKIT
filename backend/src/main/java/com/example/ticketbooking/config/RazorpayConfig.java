package com.example.ticketbooking.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Razorpay API credentials loaded from application configuration.
 * Exposes keyId and keySecret for use in PaymentService.
 */
@Component
@Getter
public class RazorpayConfig {

    @Value("${razorpay.key-id:rzp_test_placeholder}")
    private String keyId;

    @Value("${razorpay.key-secret:placeholder_secret}")
    private String keySecret;
}
