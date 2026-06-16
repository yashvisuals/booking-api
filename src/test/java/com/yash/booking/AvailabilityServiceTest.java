package com.yash.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yash.booking.domain.AvailabilityRule;
import com.yash.booking.domain.Booking;
import com.yash.booking.domain.Role;
import com.yash.booking.domain.User;
import com.yash.booking.repo.AvailabilityRuleRepository;
import com.yash.booking.repo.BookingRepository;
import com.yash.booking.repo.UserRepository;
import com.yash.booking.service.AvailabilityService;
import com.yash.booking.web.dto.SlotResponse;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AvailabilityServiceTest {

    @Autowired
    AvailabilityService service;
    @Autowired
    AvailabilityRuleRepository rules;
    @Autowired
    BookingRepository bookings;
    @Autowired
    UserRepository users;

    // a Tuesday
    private static final LocalDate TUESDAY = LocalDate.parse("2026-06-23");

    @BeforeEach
    void clean() {
        bookings.deleteAll();
        rules.deleteAll();
        users.deleteAll();
    }

    @Test
    void generatesSlotsConvertedFromProviderZoneToUtc() {
        User provider = users.save(new User("p@test.com", "pw", Role.PROVIDER));
        rules.save(new AvailabilityRule(provider, DayOfWeek.TUESDAY,
            LocalTime.of(9, 0), LocalTime.of(12, 0), "Asia/Kolkata", 30));

        List<SlotResponse> slots = service.freeSlots(provider.getId(), TUESDAY);

        // 09:00–12:00 in 30-min slots = 6 slots
        assertEquals(6, slots.size());
        // 09:00 IST (UTC+5:30) == 03:30 UTC
        assertEquals(Instant.parse("2026-06-23T03:30:00Z"), slots.get(0).start());
        assertEquals(Instant.parse("2026-06-23T04:00:00Z"), slots.get(0).end());
    }

    @Test
    void removesAlreadyBookedSlots() {
        User provider = users.save(new User("p@test.com", "pw", Role.PROVIDER));
        User customer = users.save(new User("c@test.com", "pw", Role.CUSTOMER));
        rules.save(new AvailabilityRule(provider, DayOfWeek.TUESDAY,
            LocalTime.of(9, 0), LocalTime.of(12, 0), "Asia/Kolkata", 30));

        bookings.save(new Booking(provider, customer,
            Instant.parse("2026-06-23T03:30:00Z"), Instant.parse("2026-06-23T04:00:00Z")));

        List<SlotResponse> slots = service.freeSlots(provider.getId(), TUESDAY);

        assertEquals(5, slots.size());
        // 09:00 slot is taken, so first free is 09:30 IST == 04:00 UTC
        assertEquals(Instant.parse("2026-06-23T04:00:00Z"), slots.get(0).start());
    }
}
