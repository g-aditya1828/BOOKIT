package com.example.ticketbooking.controller;

import com.example.ticketbooking.dto.ContentDto;
import com.example.ticketbooking.model.enums.ContentType;
import com.example.ticketbooking.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContentController {

    private final ContentService contentService;

    @GetMapping
    public ResponseEntity<List<ContentDto>> getContent(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ContentType type) {
        if ((q != null && !q.isBlank()) || type != null) {
            return ResponseEntity.ok(contentService.search(q, type));
        }
        return ResponseEntity.ok(contentService.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentDto> getContentById(@PathVariable Long id) {
        return ResponseEntity.ok(contentService.getById(id));
    }
}
