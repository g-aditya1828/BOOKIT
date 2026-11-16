package com.example.ticketbooking.controller;

import com.example.ticketbooking.dto.ShowDto;
import com.example.ticketbooking.dto.ShowSeatDto;
import com.example.ticketbooking.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShowController {

    private final ShowService showService;

    @GetMapping
    public ResponseEntity<List<ShowDto>> getShows(
            @RequestParam(required = false) Long contentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (contentId != null && date != null) {
            return ResponseEntity.ok(showService.getShowsByContent(contentId, date));
        } else if (date != null) {
            return ResponseEntity.ok(showService.getShowsByDateRange(date, date));
        } else if (contentId != null) {
            return ResponseEntity.ok(showService.getShowsByContent(contentId, LocalDate.now()));
        }
        return ResponseEntity.ok(showService.getShowsByDateRange(LocalDate.now(), LocalDate.now().plusDays(7)));
    }

    @GetMapping("/range")
    public ResponseEntity<List<ShowDto>> getShowsRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(showService.getShowsByDateRange(from, to));
    }

    @GetMapping("/{showId}/seats")
    public ResponseEntity<List<ShowSeatDto>> getShowSeats(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getShowSeats(showId));
    }
}
