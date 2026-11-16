package com.example.ticketbooking.dto;

public record VenueDto(
        Long id,
        String name,
        String city,
        String state,
        String country,
        String address,
        Double lat,
        Double lng,
        String pincode,
        int totalScreens,
        boolean active
) {}
