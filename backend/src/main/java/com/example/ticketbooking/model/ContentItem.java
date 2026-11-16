package com.example.ticketbooking.model;

import com.example.ticketbooking.model.enums.ContentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "content_items")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    private String posterUrl;
    private String trailerUrl;
    private String language;
    private int durationMins;
    private Double rating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType contentType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "content_genres", joinColumns = @JoinColumn(name = "content_id"))
    @Column(name = "genre")
    private List<String> genres;

    private LocalDate releaseDate;

    @Builder.Default
    private boolean active = true;
}
