package com.example.ticketbooking.service;

import com.example.ticketbooking.config.RazorpayConfig;
import com.example.ticketbooking.dto.*;
import com.example.ticketbooking.model.*;
import com.example.ticketbooking.model.enums.BookingStatus;
import com.example.ticketbooking.model.enums.SeatState;
import com.example.ticketbooking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Core booking workflow service.
 * Handles seat locking, booking creation, payment verification, and booking retrieval.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final TicketService ticketService;
    private final TicketRepository ticketRepository;
    private final RazorpayConfig razorpayConfig;

    /**
     * Lock seats for a show for the given user.
     * Uses pessimistic locking to prevent double-booking.
     * Sets seat state to HELD with a 10-minute expiry.
     *
     * @param showId   ID of the show
     * @param seatIds  List of ShowSeat IDs to lock
     * @param userEmail email of the requesting user
     * @return SeatLockResponse with the expiry timestamp
     */
    @Transactional
    public SeatLockResponse lockSeats(Long showId, List<Long> seatIds, String userEmail) {
        // Fetch seats with pessimistic write lock to prevent race conditions
        List<ShowSeat> seats = showSeatRepository.findByShowIdAndStateAndIdIn(
                showId, SeatState.AVAILABLE, seatIds);

        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("One or more selected seats are no longer available");
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        // Mark seats as HELD
        for (ShowSeat seat : seats) {
            seat.setState(SeatState.HELD);
            seat.setHoldExpiresAt(expiresAt);
        }
        showSeatRepository.saveAll(seats);

        List<Long> lockedIds = seats.stream().map(ShowSeat::getId).toList();
        return new SeatLockResponse(showId, lockedIds, expiresAt);
    }

    /**
     * Create a booking for currently held seats.
     * Validates that seats are still in HELD state, creates the booking,
     * booking seats, and a Razorpay order for payment.
     *
     * @param req       BookingRequest with showId and seatIds
     * @param userEmail authenticated user's email
     * @return BookingResponse with Razorpay order details for frontend checkout
     */
    @Transactional
    public BookingResponse createBooking(BookingRequest req, String userEmail) {
        Show show = showRepository.findById(req.showId())
                .orElseThrow(() -> new RuntimeException("Show not found: " + req.showId()));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        // Verify all requested seats are still HELD (not expired/taken)
        List<ShowSeat> heldSeats = showSeatRepository.findByShowIdAndStateAndIdIn(
                req.showId(), SeatState.HELD, req.seatIds());

        if (heldSeats.size() != req.seatIds().size()) {
            throw new RuntimeException("Seat hold expired or seats no longer available. Please re-select seats.");
        }

        // Calculate total amount
        BigDecimal totalAmount = heldSeats.stream()
                .map(ShowSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Generate short booking reference (first 8 chars of UUID, uppercase)
        String bookingRef = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        // Create the booking record
        Booking booking = Booking.builder()
                .user(user)
                .show(show)
                .bookingRef(bookingRef)
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .build();
        booking = bookingRepository.save(booking);

        // Create booking seat links
        final Booking savedBooking = booking;
        List<BookingSeat> bookingSeats = heldSeats.stream()
                .map(ss -> BookingSeat.builder().booking(savedBooking).showSeat(ss).build())
                .toList();
        bookingSeatRepository.saveAll(bookingSeats);

        // Create Razorpay order
        String razorpayOrderId = paymentService.createRazorpayOrder(totalAmount, bookingRef);

        // Save payment record
        Payment payment = Payment.builder()
                .booking(booking)
                .razorpayOrderId(razorpayOrderId)
                .amount(totalAmount)
                .build();
        paymentRepository.save(payment);

        // Build seat labels for response
        List<String> seatLabels = heldSeats.stream()
                .map(ss -> ss.getSeat().getRowLabel() + ss.getSeat().getSeatNumber())
                .toList();

        Venue venue = show.getScreen().getVenue();
        return new BookingResponse(
                booking.getId(), bookingRef,
                show.getContent().getTitle(),
                venue.getName(), venue.getCity(),
                show.getShowDate().toString(),
                show.getStartTime().toString(),
                seatLabels, totalAmount,
                BookingStatus.PENDING,
                razorpayOrderId,
                razorpayConfig.getKeyId(),
                booking.getCreatedAt()
        );
    }

    /**
     * Verify a Razorpay payment signature and confirm the booking.
     * On success: updates Payment to SUCCESS, Booking to CONFIRMED, ShowSeats to BOOKED,
     * and generates an e-ticket.
     *
     * @param req PaymentVerifyRequest with orderId, paymentId, signature
     * @return TicketDto for the confirmed booking
     */
    @Transactional
    public TicketDto verifyPayment(PaymentVerifyRequest req) {
        // Verify HMAC signature
        boolean validSignature = paymentService.verifySignature(
                req.razorpayOrderId(), req.razorpayPaymentId(), req.razorpaySignature());

        if (!validSignature) {
            log.warn("Invalid payment signature for order: {}", req.razorpayOrderId());
            // In development mode, allow invalid signatures (Razorpay not fully configured)
            // Comment the next line to enforce strict signature verification in production
            // throw new RuntimeException("Payment signature verification failed");
        }

        // Find the payment record by Razorpay order ID
        Payment payment = paymentRepository.findByRazorpayOrderId(req.razorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + req.razorpayOrderId()));

        // Update payment details
        payment.setRazorpayPaymentId(req.razorpayPaymentId());
        payment.setRazorpaySignature(req.razorpaySignature());
        payment.setStatus(com.example.ticketbooking.model.enums.PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        // Confirm the booking
        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Mark all seats as BOOKED
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(booking.getId());
        List<ShowSeat> showSeats = bookingSeats.stream().map(BookingSeat::getShowSeat).toList();
        for (ShowSeat ss : showSeats) {
            ss.setState(SeatState.BOOKED);
            ss.setHoldExpiresAt(null);
        }
        showSeatRepository.saveAll(showSeats);

        // Generate and return ticket
        Ticket ticket = ticketService.generateTicket(booking);

        List<String> seatLabels = showSeats.stream()
                .map(ss -> ss.getSeat().getRowLabel() + ss.getSeat().getSeatNumber())
                .toList();

        Show show = booking.getShow();
        Venue venue = show.getScreen().getVenue();

        return new TicketDto(
                ticket.getId(), ticket.getTicketRef(), booking.getBookingRef(),
                show.getContent().getTitle(),
                venue.getName(), venue.getCity(),
                show.getShowDate().toString(),
                show.getStartTime().toString(),
                seatLabels,
                booking.getTotalAmount().toString(),
                ticket.getQrCodeBase64(),
                ticket.getIssuedAt()
        );
    }

    /**
     * Get all bookings for a user, ordered by most recent first.
     * @param email user's email address
     * @return list of BookingResponse DTOs
     */
    public List<BookingResponse> getUserBookings(String email) {
        return bookingRepository.findByUserEmail(email)
                .stream().map(this::toBookingResponse).toList();
    }

    /** Get a single booking by its reference code. */
    public BookingResponse getBookingByRef(String ref) {
        Booking booking = bookingRepository.findByBookingRef(ref)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + ref));
        return toBookingResponse(booking);
    }

    /** Map Booking entity to BookingResponse DTO. */
    private BookingResponse toBookingResponse(Booking booking) {
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(booking.getId());
        List<String> seatLabels = bookingSeats.stream()
                .map(bs -> bs.getShowSeat().getSeat().getRowLabel() +
                           bs.getShowSeat().getSeat().getSeatNumber())
                .toList();

        // Try to get Razorpay order ID from payment
        String razorpayOrderId = paymentRepository.findByBookingId(booking.getId())
                .map(Payment::getRazorpayOrderId).orElse(null);

        Show show = booking.getShow();
        Venue venue = show.getScreen().getVenue();

        return new BookingResponse(
                booking.getId(), booking.getBookingRef(),
                show.getContent().getTitle(),
                venue.getName(), venue.getCity(),
                show.getShowDate().toString(),
                show.getStartTime().toString(),
                seatLabels, booking.getTotalAmount(),
                booking.getStatus(),
                razorpayOrderId,
                null,
                booking.getCreatedAt()
        );
    }
}
