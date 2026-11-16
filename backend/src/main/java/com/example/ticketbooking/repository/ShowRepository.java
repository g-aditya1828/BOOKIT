package com.example.ticketbooking.repository;

import com.example.ticketbooking.model.Show;
import com.example.ticketbooking.model.enums.ShowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByContentIdAndShowDateAndStatusNot(Long contentId, LocalDate showDate, ShowStatus status);

    List<Show> findByShowDateBetween(LocalDate from, LocalDate to);

    List<Show> findByScreenIdAndShowDate(Long screenId, LocalDate showDate);

    long countByStatus(ShowStatus status);
}
