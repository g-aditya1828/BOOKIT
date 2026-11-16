package com.example.ticketbooking.dto;

import java.util.List;

public record BookingRequest(
        Long showId,
        List<Long> seatIds
) {}
