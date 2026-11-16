package com.example.ticketbooking.service;

import com.example.ticketbooking.dto.VenueDto;
import com.example.ticketbooking.model.Venue;
import com.example.ticketbooking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Manages cinema venues.
 * Includes Haversine-based geolocation sorting for the getNearby() feature.
 */
@Service
@RequiredArgsConstructor
public class VenueService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final VenueRepository venueRepository;

    /** Return all active venues. */
    public List<VenueDto> getAll() {
        return venueRepository.findByActiveTrue()
                .stream().map(this::toDto).toList();
    }

    /** Return a venue by its ID. */
    public VenueDto getById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found: " + id));
        return toDto(venue);
    }

    /** Return all active venues in a given city (case-insensitive). */
    public List<VenueDto> getByCity(String city) {
        return venueRepository.findByCityIgnoreCase(city)
                .stream().filter(Venue::isActive).map(this::toDto).toList();
    }

    /**
     * Return all active venues sorted by distance from the given coordinates.
     * Uses the Haversine formula: dist = 2R * asin(sqrt(sin²(Δlat/2) + cos(lat1)*cos(lat2)*sin²(Δlng/2)))
     */
    public List<VenueDto> getNearby(Double lat, Double lng) {
        return venueRepository.findByActiveTrue()
                .stream()
                .filter(v -> v.getLat() != null && v.getLng() != null)
                .sorted(Comparator.comparingDouble(v -> haversineDistance(lat, lng, v.getLat(), v.getLng())))
                .map(this::toDto)
                .toList();
    }

    /** Create a new venue. */
    @Transactional
    public VenueDto create(VenueDto dto) {
        Venue venue = Venue.builder()
                .name(dto.name())
                .city(dto.city())
                .state(dto.state())
                .country(dto.country())
                .address(dto.address())
                .lat(dto.lat())
                .lng(dto.lng())
                .pincode(dto.pincode())
                .totalScreens(dto.totalScreens())
                .active(true)
                .build();
        return toDto(venueRepository.save(venue));
    }

    /**
     * Calculate the Haversine distance in kilometres between two lat/lng points.
     * @return distance in km
     */
    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_RADIUS_KM * c;
    }

    /** Map Venue entity to VenueDto. */
    private VenueDto toDto(Venue v) {
        return new VenueDto(v.getId(), v.getName(), v.getCity(), v.getState(),
                v.getCountry(), v.getAddress(), v.getLat(), v.getLng(),
                v.getPincode(), v.getTotalScreens(), v.isActive());
    }
}
