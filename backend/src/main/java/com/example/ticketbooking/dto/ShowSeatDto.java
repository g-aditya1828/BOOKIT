package com.example.ticketbooking.dto;

import com.example.ticketbooking.model.enums.SeatCategory;
import com.example.ticketbooking.model.enums.SeatState;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShowSeatDto(
        Long id,
        Long seatId,
        String rowLabel,
        int seatNumber,
        SeatCategory category,
        SeatState state,
        BigDecimal price,
        LocalDateTime holdExpiresAt
) {}
