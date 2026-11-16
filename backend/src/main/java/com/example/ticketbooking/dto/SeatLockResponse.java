package com.example.ticketbooking.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SeatLockResponse(
        Long showId,
        List<Long> lockedSeatIds,
        LocalDateTime expiresAt
) {}
