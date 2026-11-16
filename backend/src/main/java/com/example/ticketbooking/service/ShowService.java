package com.example.ticketbooking.service;

import com.example.ticketbooking.dto.ShowDto;
import com.example.ticketbooking.dto.ShowSeatDto;
import com.example.ticketbooking.model.*;
import com.example.ticketbooking.model.enums.SeatCategory;
import com.example.ticketbooking.model.enums.ShowStatus;
import com.example.ticketbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages show scheduling and seat availability.
 * When a show is created, ShowSeat rows are automatically generated for every seat in the screen.
 */
@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ContentItemRepository contentItemRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;

    /**
     * Get all shows for a specific content item on a given date.
     * Excludes cancelled shows.
     */
    public List<ShowDto> getShowsByContent(Long contentId, LocalDate date) {
        return showRepository
                .findByContentIdAndShowDateAndStatusNot(contentId, date, ShowStatus.CANCELLED)
                .stream().map(this::toDto).toList();
    }

    /** Get all shows within a date range. */
    public List<ShowDto> getShowsByDateRange(LocalDate from, LocalDate to) {
        return showRepository.findByShowDateBetween(from, to)
                .stream().map(this::toDto).toList();
    }

    /**
     * Get all ShowSeat entries for a show, mapped to DTOs with current state.
     * The UI uses state to render seats as available/held/booked.
     */
    public List<ShowSeatDto> getShowSeats(Long showId) {
        return showSeatRepository.findByShowId(showId)
                .stream().map(this::toShowSeatDto).toList();
    }

    /**
     * Create a new show and automatically generate ShowSeat rows for all seats in the screen.
     * Price multipliers per category: PLATINUM=1.5x, GOLD=1.25x, SILVER=1.0x.
     */
    @Transactional
    public ShowDto createShow(ShowDto dto) {
        ContentItem content = contentItemRepository.findById(dto.contentId())
                .orElseThrow(() -> new RuntimeException("Content not found: " + dto.contentId()));
        Screen screen = screenRepository.findById(dto.screenId())
                .orElseThrow(() -> new RuntimeException("Screen not found: " + dto.screenId()));

        Show show = Show.builder()
                .content(content)
                .screen(screen)
                .showDate(dto.showDate())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .basePrice(dto.basePrice())
                .status(ShowStatus.SCHEDULED)
                .language(dto.language() != null ? dto.language() : content.getLanguage())
                .build();
        show = showRepository.save(show);

        // Generate ShowSeat entries for all seats in the screen
        List<Seat> seats = seatRepository.findByScreenId(screen.getId());
        List<ShowSeat> showSeats = new ArrayList<>();
        for (Seat seat : seats) {
            BigDecimal price = computeSeatPrice(dto.basePrice(), seat.getCategory());
            showSeats.add(ShowSeat.builder()
                    .show(show)
                    .seat(seat)
                    .price(price)
                    .build());
        }
        showSeatRepository.saveAll(showSeats);

        return toDto(show);
    }

    /** Apply category price multiplier to base price. */
    private BigDecimal computeSeatPrice(BigDecimal base, SeatCategory category) {
        return switch (category) {
            case PLATINUM -> base.multiply(new BigDecimal("1.5"));
            case GOLD -> base.multiply(new BigDecimal("1.25"));
            case SILVER -> base;
        };
    }

    /** Map Show entity to ShowDto. */
    private ShowDto toDto(Show s) {
        Screen screen = s.getScreen();
        Venue venue = screen.getVenue();
        return new ShowDto(
                s.getId(),
                s.getContent().getId(),
                s.getContent().getTitle(),
                screen.getId(),
                screen.getScreenName(),
                venue.getId(),
                venue.getName(),
                venue.getCity(),
                s.getShowDate(),
                s.getStartTime(),
                s.getEndTime(),
                s.getBasePrice(),
                s.getStatus(),
                s.getLanguage()
        );
    }

    /** Map ShowSeat entity to ShowSeatDto. */
    private ShowSeatDto toShowSeatDto(ShowSeat ss) {
        Seat seat = ss.getSeat();
        return new ShowSeatDto(
                ss.getId(),
                seat.getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                seat.getCategory(),
                ss.getState(),
                ss.getPrice(),
                ss.getHoldExpiresAt()
        );
    }
}
