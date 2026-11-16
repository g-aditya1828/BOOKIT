package com.example.ticketbooking.controller;

import com.example.ticketbooking.dto.AdminStatsDto;
import com.example.ticketbooking.dto.ContentDto;
import com.example.ticketbooking.dto.VenueDto;
import com.example.ticketbooking.model.Booking;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.model.Venue;
import com.example.ticketbooking.service.AdminService;
import com.example.ticketbooking.service.ContentService;
import com.example.ticketbooking.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;
    private final ContentService contentService;
    private final VenueService venueService;

    /**
     * Get platform statistics:
     * - Number of users
     * - Number of bookings
     * - Number of cinemas over the country
     * - Total revenue
     * - Active shows and daily activity
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getPlatformStats());
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getRecentBookings(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(adminService.getRecentBookings(limit));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(PageRequest.of(page, size)));
    }

    @GetMapping("/venues")
    public ResponseEntity<List<Venue>> getVenues() {
        return ResponseEntity.ok(adminService.getAllVenues());
    }

    @PostMapping("/content")
    public ResponseEntity<ContentDto> createContent(@RequestBody ContentDto contentDto) {
        return ResponseEntity.ok(contentService.create(contentDto));
    }

    @PostMapping("/venues")
    public ResponseEntity<VenueDto> createVenue(@RequestBody VenueDto venueDto) {
        return ResponseEntity.ok(venueService.create(venueDto));
    }
}
