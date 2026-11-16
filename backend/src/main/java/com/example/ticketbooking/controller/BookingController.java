package com.example.ticketbooking.controller;

import com.example.ticketbooking.dto.*;
import com.example.ticketbooking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    private String resolveEmail(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "customer@bookit.com";
    }

    @PostMapping("/bookings/lock-seats")
    public ResponseEntity<SeatLockResponse> lockSeats(
            @RequestBody SeatLockRequest request,
            Authentication auth) {
        String email = resolveEmail(auth);
        return ResponseEntity.ok(bookingService.lockSeats(request.showId(), request.seatIds(), email));
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingRequest request,
            Authentication auth) {
        String email = resolveEmail(auth);
        return ResponseEntity.ok(bookingService.createBooking(request, email));
    }

    @PostMapping("/payments/verify")
    public ResponseEntity<TicketDto> verifyPayment(@RequestBody PaymentVerifyRequest request) {
        return ResponseEntity.ok(bookingService.verifyPayment(request));
    }

    @GetMapping("/bookings/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication auth) {
        String email = resolveEmail(auth);
        return ResponseEntity.ok(bookingService.getUserBookings(email));
    }

    @GetMapping("/bookings/{ref}")
    public ResponseEntity<BookingResponse> getBookingByRef(@PathVariable String ref) {
        return ResponseEntity.ok(bookingService.getBookingByRef(ref));
    }

    // Compatibility endpoint for simple seat lock
    @PostMapping("/seat-locks")
    public ResponseEntity<SeatLockResponse> simpleSeatLock(
            @RequestBody SeatLockRequest request,
            Authentication auth) {
        String email = resolveEmail(auth);
        return ResponseEntity.ok(bookingService.lockSeats(request.showId(), request.seatIds(), email));
    }
}
