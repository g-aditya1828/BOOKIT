package com.example.ticketbooking.config;

import com.example.ticketbooking.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled task that periodically releases expired seat holds back to AVAILABLE state.
 * Runs every 60 seconds.
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class HoldExpiryScheduler {

    private final ShowSeatRepository showSeatRepository;

    /**
     * Release all ShowSeat rows where state = HELD and holdExpiresAt < now.
     * Runs every 60,000 milliseconds (1 minute).
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void releaseExpiredHolds() {
        int released = showSeatRepository.updateExpiredHolds(LocalDateTime.now());
        if (released > 0) {
            log.info("Released {} expired seat holds", released);
        }
    }
}
