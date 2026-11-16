package com.example.ticketbooking.dto;

import com.example.ticketbooking.model.enums.ContentType;

import java.time.LocalDate;
import java.util.List;

public record ContentDto(
        Long id,
        String title,
        String description,
        String posterUrl,
        String trailerUrl,
        String language,
        int durationMins,
        Double rating,
        ContentType contentType,
        List<String> genres,
        LocalDate releaseDate,
        boolean active
) {}
