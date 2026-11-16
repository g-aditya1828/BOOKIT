package com.example.ticketbooking.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "venues")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String city;
    private String state;
    private String country;
    private String address;
    private Double lat;
    private Double lng;
    private String pincode;
    private int totalScreens;

    @Builder.Default
    private boolean active = true;
}
