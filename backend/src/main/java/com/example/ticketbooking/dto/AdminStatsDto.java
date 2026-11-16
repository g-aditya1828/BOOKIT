package com.example.ticketbooking.dto;

import java.math.BigDecimal;

public record AdminStatsDto(
        long totalUsers,
        long totalBookings,
        BigDecimal totalRevenue,
        long totalVenues,
        long activeShows,
        long bookingsToday,
        BigDecimal revenueToday,
        long totalMovies
) {}
