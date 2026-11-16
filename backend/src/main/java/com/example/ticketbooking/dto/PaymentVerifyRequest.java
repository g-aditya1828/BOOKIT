package com.example.ticketbooking.dto;

public record PaymentVerifyRequest(
        String razorpayOrderId,
        String razorpayPaymentId,
        String razorpaySignature
) {}
