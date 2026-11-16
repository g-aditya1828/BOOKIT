package com.example.ticketbooking.controller;

import com.example.ticketbooking.dto.VenueDto;
import com.example.ticketbooking.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VenueController {

    private final VenueService venueService;

    @GetMapping
    public ResponseEntity<List<VenueDto>> getAllVenues(@RequestParam(required = false) String city) {
        if (city != null && !city.isBlank()) {
            return ResponseEntity.ok(venueService.getByCity(city));
        }
        return ResponseEntity.ok(venueService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueDto> getVenueById(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.getById(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<VenueDto>> getNearbyVenues(
            @RequestParam Double lat,
            @RequestParam Double lng) {
        return ResponseEntity.ok(venueService.getNearby(lat, lng));
    }
}
