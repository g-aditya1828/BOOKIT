package com.example.ticketbooking.config;

import com.example.ticketbooking.model.*;
import com.example.ticketbooking.model.enums.*;
import com.example.ticketbooking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ContentItemRepository contentRepo;
    private final VenueRepository venueRepo;
    private final ScreenRepository screenRepo;
    private final SeatRepository seatRepo;
    private final ShowRepository showRepo;
    private final ShowSeatRepository showSeatRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Checking and seeding BOOKIT database...");

        // 1. Seed Users (Admin and Customer)
        if (userRepo.count() == 0) {
            User admin = User.builder()
                    .name("Platform Admin")
                    .email("admin@bookit.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .role("ADMINISTRATOR")
                    .active(true)
                    .build();

            User customer = User.builder()
                    .name("Akanksha Thakur")
                    .email("customer@bookit.com")
                    .passwordHash(passwordEncoder.encode("Customer@123"))
                    .role("CUSTOMER")
                    .active(true)
                    .build();

            userRepo.saveAll(List.of(admin, customer));
            log.info("Seeded default users: admin@bookit.com and customer@bookit.com");
        }

        // 2. Seed Venues across India
        if (venueRepo.count() == 0) {
            List<Venue> venues = List.of(
                    Venue.builder().name("Cinema Stela").city("Mumbai").state("Maharashtra").country("India").address("Bandra West, Hill Road").lat(19.0596).lng(72.8295).pincode("400050").totalScreens(3).active(true).build(),
                    Venue.builder().name("PVR Superplex").city("Delhi").state("Delhi").country("India").address("Logix City Center, Noida Sector 32").lat(28.5747).lng(77.3561).pincode("201301").totalScreens(5).active(true).build(),
                    Venue.builder().name("Inox Grand Theatre").city("Bengaluru").state("Karnataka").country("India").address("Garuda Mall, Magrath Road").lat(12.9702).lng(77.6094).pincode("560025").totalScreens(4).active(true).build(),
                    Venue.builder().name("Cinepolis Nexus").city("Hyderabad").state("Telangana").country("India").address("Kukatpally, KPHB Colony").lat(17.4938).lng(78.3995).pincode("500072").totalScreens(6).active(true).build(),
                    Venue.builder().name("SPI Palazzo Cinemas").city("Chennai").state("Tamil Nadu").country("India").address("Nexus Vijaya Mall, Vadapalani").lat(13.0500).lng(80.2121).pincode("600026").totalScreens(4).active(true).build()
            );
            venueRepo.saveAll(venues);
            log.info("Seeded 5 nationwide cinema venues");
        }

        // 3. Seed Movies/Content
        if (contentRepo.count() == 0) {
            List<ContentItem> movies = List.of(
                    ContentItem.builder()
                            .title("Godzilla x Kong: The New Empire")
                            .description("An all-new adventure pitting the almighty Kong and the fearsome Godzilla against a colossal undiscovered threat hidden within our world.")
                            .language("English")
                            .durationMins(115)
                            .rating(8.2)
                            .contentType(ContentType.MOVIE)
                            .genres(List.of("Sci-Fi", "Action"))
                            .posterUrl("https://image.tmdb.org/t/p/w500/bQ2ywkch09pqGoG6jzWchC9N94P.jpg")
                            .releaseDate(LocalDate.now().minusDays(10))
                            .active(true)
                            .build(),
                    ContentItem.builder()
                            .title("Ghostbusters: Frozen Empire")
                            .description("The Spengler family returns to where it all started – the iconic New York City firehouse – to team up with the original Ghostbusters.")
                            .language("English")
                            .durationMins(115)
                            .rating(7.5)
                            .contentType(ContentType.MOVIE)
                            .genres(List.of("Comedy", "Fantasy", "Adventure"))
                            .posterUrl("https://image.tmdb.org/t/p/w500/e1J2oNzL0K94o3L5en49bp5bQDI.jpg")
                            .releaseDate(LocalDate.now().minusDays(15))
                            .active(true)
                            .build(),
                    ContentItem.builder()
                            .title("Arthur The King")
                            .description("An adventure racer adopts a stray dog named Arthur to join him in an epic endurance race through the jungle.")
                            .language("English")
                            .durationMins(107)
                            .rating(7.8)
                            .contentType(ContentType.MOVIE)
                            .genres(List.of("Adventure", "Drama"))
                            .posterUrl("https://image.tmdb.org/t/p/w500/gxVcBcZt1L5j8cW2G3n8W1aP5q7.jpg")
                            .releaseDate(LocalDate.now().minusDays(20))
                            .active(true)
                            .build(),
                    ContentItem.builder()
                            .title("Exhuma")
                            .description("A renowned shaman and her protégé are hired by an affluent family in Los Angeles to uncover the cause of a disturbing paranormal affliction.")
                            .language("Korean")
                            .durationMins(134)
                            .rating(8.0)
                            .contentType(ContentType.MOVIE)
                            .genres(List.of("Horror", "Mystery"))
                            .posterUrl("https://image.tmdb.org/t/p/w500/kCGlIMHnOm8JPXq3rXM6c5vP5eT.jpg")
                            .releaseDate(LocalDate.now().minusDays(25))
                            .active(true)
                            .build(),
                    ContentItem.builder()
                            .title("Kung Fu Panda 4")
                            .description("Po must train a new warrior when he's chosen to become the spiritual leader of the Valley of Peace.")
                            .language("English")
                            .durationMins(94)
                            .rating(7.6)
                            .contentType(ContentType.MOVIE)
                            .genres(List.of("Animation", "Action", "Family"))
                            .posterUrl("https://image.tmdb.org/t/p/w500/kDp1vUBnMpe8ak4rjgl3cLELqjU.jpg")
                            .releaseDate(LocalDate.now().minusDays(30))
                            .active(true)
                            .build(),
                    ContentItem.builder()
                            .title("Dune: Part Two")
                            .description("Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.")
                            .language("English")
                            .durationMins(166)
                            .rating(8.6)
                            .contentType(ContentType.MOVIE)
                            .genres(List.of("Sci-Fi", "Adventure"))
                            .posterUrl("https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg")
                            .releaseDate(LocalDate.now().minusDays(35))
                            .active(true)
                            .build()
            );
            contentRepo.saveAll(movies);
            log.info("Seeded 6 featured movies");
        }

        // 4. Create Screens & Seats if needed
        List<Venue> allVenues = venueRepo.findAll();
        for (Venue venue : allVenues) {
            List<Screen> existingScreens = screenRepo.findByVenueId(venue.getId());
            if (existingScreens.isEmpty()) {
                Screen screen = Screen.builder()
                        .venue(venue)
                        .screenName("Screen 1 (Dolby Atmos)")
                        .screenType("Dolby Atmos 4K")
                        .totalSeats(70)
                        .build();
                screen = screenRepo.save(screen);

                // Create 7 rows (A-G) with 10 seats each
                String[] rows = {"A", "B", "C", "D", "E", "F", "G"};
                List<Seat> seats = new ArrayList<>();
                for (String r : rows) {
                    SeatCategory cat = "A".equals(r) ? SeatCategory.PLATINUM : ("B".equals(r) || "C".equals(r) ? SeatCategory.GOLD : SeatCategory.SILVER);
                    for (int num = 1; num <= 10; num++) {
                        seats.add(Seat.builder().screen(screen).rowLabel(r).seatNumber(num).category(cat).build());
                    }
                }
                seatRepo.saveAll(seats);

                // Seed shows for today and next 7 days
                List<ContentItem> allMovies = contentRepo.findAll();
                LocalDate today = LocalDate.now();
                LocalTime[] times = {
                        LocalTime.of(13, 0),
                        LocalTime.of(15, 0),
                        LocalTime.of(17, 0),
                        LocalTime.of(19, 0)
                };

                for (int d = 0; d < 7; d++) {
                    LocalDate showDate = today.plusDays(d);
                    for (int t = 0; t < times.length; t++) {
                        ContentItem movie = allMovies.get((d + t) % allMovies.size());
                        Show show = Show.builder()
                                .content(movie)
                                .screen(screen)
                                .showDate(showDate)
                                .startTime(times[t])
                                .endTime(times[t].plusHours(2).plusMinutes(15))
                                .basePrice(BigDecimal.valueOf(220.0))
                                .status(ShowStatus.SCHEDULED)
                                .language(movie.getLanguage())
                                .build();
                        show = showRepo.save(show);

                        // Create ShowSeats
                        final Show savedShow = show;
                        List<ShowSeat> showSeats = seats.stream().map(s -> {
                            double multiplier = s.getCategory() == SeatCategory.PLATINUM ? 1.5 : (s.getCategory() == SeatCategory.GOLD ? 1.25 : 1.0);
                            BigDecimal price = savedShow.getBasePrice().multiply(BigDecimal.valueOf(multiplier));
                            return ShowSeat.builder()
                                    .show(savedShow)
                                    .seat(s)
                                    .price(price)
                                    .state(SeatState.AVAILABLE)
                                    .build();
                        }).toList();
                        showSeatRepo.saveAll(showSeats);
                    }
                }
            }
        }
        log.info("Database seeding completed successfully!");
    }
}
