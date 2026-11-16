package com.example.ticketbooking.service;

import com.example.ticketbooking.dto.AdminStatsDto;
import com.example.ticketbooking.model.Booking;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.model.Venue;
import com.example.ticketbooking.model.enums.ShowStatus;
import com.example.ticketbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final VenueRepository venueRepository;
    private final ShowRepository showRepository;
    private final ContentItemRepository contentItemRepository;

    public AdminStatsDto getPlatformStats() {
        long totalUsers = userRepository.count();
        long totalBookings = bookingRepository.count();
        long totalVenues = venueRepository.countByActiveTrue();
        long totalMovies = contentItemRepository.count();
        long activeShows = showRepository.countByStatus(ShowStatus.SCHEDULED);

        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        long bookingsToday = bookingRepository.countByCreatedAtBetween(startOfToday, endOfToday);

        BigDecimal totalRevenue = bookingRepository.sumConfirmedRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        BigDecimal revenueToday = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ? totalRevenue.multiply(BigDecimal.valueOf(0.15)) : BigDecimal.ZERO;

        return new AdminStatsDto(
                totalUsers,
                totalBookings,
                totalRevenue,
                totalVenues,
                activeShows,
                bookingsToday,
                revenueToday,
                totalMovies
        );
    }

    public List<Booking> getRecentBookings(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return bookingRepository.findAll(pageable).getContent();
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }
}
