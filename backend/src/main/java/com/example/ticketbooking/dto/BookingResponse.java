package com.example.ticketbooking.dto;

import com.example.ticketbooking.model.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long bookingId,
        String bookingRef,
        String movieTitle,
        String venueName,
        String venueCity,
        String showDate,
        String showTime,
        List<String> seats,
        BigDecimal totalAmount,
        BookingStatus status,
        String razorpayOrderId,
        String razorpayKeyId,
        LocalDateTime createdAt
) {}
