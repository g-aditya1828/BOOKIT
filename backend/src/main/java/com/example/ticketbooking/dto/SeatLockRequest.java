package com.example.ticketbooking.dto;

import java.util.List;

public record SeatLockRequest(
        Long showId,
        List<Long> seatIds
) {}
