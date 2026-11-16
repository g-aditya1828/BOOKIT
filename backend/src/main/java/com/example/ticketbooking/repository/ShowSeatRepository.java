package com.example.ticketbooking.repository;

import com.example.ticketbooking.model.ShowSeat;
import com.example.ticketbooking.model.enums.SeatState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowId(Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.state = :state AND ss.id IN :ids")
    List<ShowSeat> findByShowIdAndStateAndIdIn(
            @Param("showId") Long showId,
            @Param("state") SeatState state,
            @Param("ids") List<Long> ids);

    long countByShowIdAndState(Long showId, SeatState state);

    @Modifying
    @Query("UPDATE ShowSeat s SET s.state = 'AVAILABLE', s.holdExpiresAt = null WHERE s.state = 'HELD' AND s.holdExpiresAt < :now")
    int updateExpiredHolds(@Param("now") LocalDateTime now);
}
