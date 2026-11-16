package com.example.ticketbooking.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TicketDto(
        Long ticketId,
        String ticketRef,
        String bookingRef,
        String movieTitle,
        String venueName,
        String venueCity,
        String showDate,
        String showTime,
        List<String> seats,
        String totalAmount,
        String qrCodeBase64,
        LocalDateTime issuedAt
) {}
