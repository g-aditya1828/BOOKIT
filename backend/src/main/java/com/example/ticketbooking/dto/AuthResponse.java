package com.example.ticketbooking.dto;

public record AuthResponse(
        String token,
        String email,
        String name,
        String role
) {}
