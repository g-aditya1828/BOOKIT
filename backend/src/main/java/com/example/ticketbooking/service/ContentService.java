package com.example.ticketbooking.service;

import com.example.ticketbooking.dto.ContentDto;
import com.example.ticketbooking.model.ContentItem;
import com.example.ticketbooking.model.enums.ContentType;
import com.example.ticketbooking.repository.ContentItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages content items (movies, live shows, sports events).
 * Provides CRUD and search operations.
 */
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentItemRepository contentItemRepository;

    /** Retrieve all active content items. */
    public List<ContentDto> getAllActive() {
        return contentItemRepository.findByActiveTrue()
                .stream().map(this::toDto).toList();
    }

    /** Retrieve a single content item by its ID. */
    public ContentDto getById(Long id) {
        ContentItem item = contentItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found: " + id));
        return toDto(item);
    }

    /**
     * Search content by title keyword and optional type filter.
     * If query is blank and type is null, returns all active items.
     */
    public List<ContentDto> search(String query, ContentType type) {
        List<ContentItem> results;

        if (type != null) {
            results = contentItemRepository.findByContentType(type)
                    .stream().filter(ContentItem::isActive).toList();
            if (query != null && !query.isBlank()) {
                String lq = query.toLowerCase();
                results = results.stream()
                        .filter(c -> c.getTitle().toLowerCase().contains(lq))
                        .toList();
            }
        } else if (query != null && !query.isBlank()) {
            results = contentItemRepository.findByTitleContainingIgnoreCaseAndActiveTrue(query);
        } else {
            results = contentItemRepository.findByActiveTrue();
        }

        return results.stream().map(this::toDto).toList();
    }

    /** Create a new content item from DTO data. */
    @Transactional
    public ContentDto create(ContentDto dto) {
        ContentItem item = ContentItem.builder()
                .title(dto.title())
                .description(dto.description())
                .posterUrl(dto.posterUrl())
                .trailerUrl(dto.trailerUrl())
                .language(dto.language())
                .durationMins(dto.durationMins())
                .rating(dto.rating())
                .contentType(dto.contentType())
                .genres(dto.genres())
                .releaseDate(dto.releaseDate())
                .active(true)
                .build();
        return toDto(contentItemRepository.save(item));
    }

    /** Update an existing content item. */
    @Transactional
    public ContentDto update(Long id, ContentDto dto) {
        ContentItem item = contentItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found: " + id));
        item.setTitle(dto.title());
        item.setDescription(dto.description());
        item.setPosterUrl(dto.posterUrl());
        item.setTrailerUrl(dto.trailerUrl());
        item.setLanguage(dto.language());
        item.setDurationMins(dto.durationMins());
        item.setRating(dto.rating());
        item.setContentType(dto.contentType());
        item.setGenres(dto.genres());
        item.setReleaseDate(dto.releaseDate());
        item.setActive(dto.active());
        return toDto(contentItemRepository.save(item));
    }

    /** Soft-delete a content item by setting active = false. */
    @Transactional
    public void deleteById(Long id) {
        ContentItem item = contentItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found: " + id));
        item.setActive(false);
        contentItemRepository.save(item);
    }

    /** Map ContentItem entity to ContentDto. */
    private ContentDto toDto(ContentItem item) {
        return new ContentDto(
                item.getId(), item.getTitle(), item.getDescription(),
                item.getPosterUrl(), item.getTrailerUrl(), item.getLanguage(),
                item.getDurationMins(), item.getRating(), item.getContentType(),
                item.getGenres(), item.getReleaseDate(), item.isActive()
        );
    }
}
