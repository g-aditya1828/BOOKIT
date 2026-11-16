package com.example.ticketbooking.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tickets")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(unique = true, nullable = false)
    private String ticketRef;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String qrCodeBase64;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime issuedAt;
}
