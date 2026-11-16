package com.example.ticketbooking.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "screens")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private String screenName;

    private int totalSeats;
    private String screenType;
}
