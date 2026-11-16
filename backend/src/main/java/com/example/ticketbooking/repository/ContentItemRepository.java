package com.example.ticketbooking.repository;

import com.example.ticketbooking.model.ContentItem;
import com.example.ticketbooking.model.enums.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentItemRepository extends JpaRepository<ContentItem, Long> {

    List<ContentItem> findByActiveTrue();

    List<ContentItem> findByTitleContainingIgnoreCaseAndActiveTrue(String title);

    List<ContentItem> findByContentType(ContentType contentType);
}
