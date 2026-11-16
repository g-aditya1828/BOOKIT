package com.example.ticketbooking.dto;

import com.example.ticketbooking.model.enums.ShowStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowDto(
        Long id,
        Long contentId,
        String contentTitle,
        Long screenId,
        String screenName,
        Long venueId,
        String venueName,
        String venueCity,
        LocalDate showDate,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal basePrice,
        ShowStatus status,
        String language
) {}
