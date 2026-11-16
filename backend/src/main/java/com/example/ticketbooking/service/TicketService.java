package com.example.ticketbooking.service;

import com.example.ticketbooking.model.Booking;
import com.example.ticketbooking.model.BookingSeat;
import com.example.ticketbooking.model.ShowSeat;
import com.example.ticketbooking.model.Ticket;
import com.example.ticketbooking.repository.BookingSeatRepository;
import com.example.ticketbooking.repository.TicketRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Generates e-tickets with QR codes for confirmed bookings.
 * Uses ZXing to produce QR PNG bytes, then encodes as Base64.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    private final TicketRepository ticketRepository;
    private final BookingSeatRepository bookingSeatRepository;

    /**
     * Generate a ticket for a confirmed booking.
     * Creates a unique ticketRef, generates a QR code encoding booking details,
     * saves the Ticket entity and returns it.
     */
    @Transactional
    public Ticket generateTicket(Booking booking) {
        // Generate unique 6-character alphanumeric ticket reference
        String ticketRef = "BK-" + randomAlphaNumeric(6);

        // Build seat label list for QR content
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(booking.getId());
        List<String> seatLabels = bookingSeats.stream()
                .map(bs -> {
                    ShowSeat ss = bs.getShowSeat();
                    return ss.getSeat().getRowLabel() + ss.getSeat().getSeatNumber();
                }).toList();

        // Build JSON payload for QR code
        String qrContent = String.format(
                "{\"ref\":\"%s\",\"movie\":\"%s\",\"venue\":\"%s\",\"date\":\"%s\",\"time\":\"%s\",\"seats\":%s}",
                ticketRef,
                escapeJson(booking.getShow().getContent().getTitle()),
                escapeJson(booking.getShow().getScreen().getVenue().getName()),
                booking.getShow().getShowDate().toString(),
                booking.getShow().getStartTime().toString(),
                seatLabels.toString()
        );

        // Generate QR code as Base64 PNG
        String qrBase64 = generateQrCodeBase64(qrContent);

        Ticket ticket = Ticket.builder()
                .booking(booking)
                .ticketRef(ticketRef)
                .qrCodeBase64(qrBase64)
                .build();

        return ticketRepository.save(ticket);
    }

    /**
     * Generate a QR code PNG and return it as a Base64-encoded string.
     * Uses ZXing QRCodeWriter with 300x300 pixel output.
     */
    private String generateQrCodeBase64(String content) {
        try {
            QRCodeWriter qrWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.MARGIN, 1,
                    EncodeHintType.CHARACTER_SET, "UTF-8"
            );
            BitMatrix matrix = qrWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300, hints);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("QR code generation failed: {}", e.getMessage());
            return "";
        }
    }

    /** Generate a random alphanumeric string of the given length. */
    private String randomAlphaNumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    /** Simple JSON string escaper for special characters. */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
