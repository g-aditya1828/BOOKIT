package com.example.ticketbooking.repository;

import com.example.ticketbooking.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findByActiveTrue();

    List<Venue> findByCityIgnoreCase(String city);

    long countByActiveTrue();
}
