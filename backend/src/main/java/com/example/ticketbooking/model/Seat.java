package com.example.ticketbooking.model;

import com.example.ticketbooking.model.enums.SeatCategory;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "seats")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(nullable = false)
    private String rowLabel;

    private int seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatCategory category;
}
