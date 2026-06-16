package com.yash.booking.service;

import com.yash.booking.domain.AvailabilityRule;
import com.yash.booking.domain.Booking;
import com.yash.booking.domain.BookingStatus;
import com.yash.booking.domain.User;
import com.yash.booking.repo.AvailabilityRuleRepository;
import com.yash.booking.repo.BookingRepository;
import com.yash.booking.repo.UserRepository;
import com.yash.booking.web.dto.CreateBookingRequest;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookingService {

    private final BookingRepository bookings;
    private final AvailabilityRuleRepository rules;
    private final UserRepository users;

    public BookingService(BookingRepository bookings, AvailabilityRuleRepository rules,
                          UserRepository users) {
        this.bookings = bookings;
        this.rules = rules;
        this.users = users;
    }

    @Transactional
    public Booking book(String customerEmail, CreateBookingRequest req) {
        User customer = users.findByEmail(customerEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unknown user"));
        User provider = users.findById(req.providerId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "provider not found"));

        // The requested start must line up with one of the provider's slots.
        Instant end = matchingSlotEnd(provider, req.start())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "requested time is not an available slot"));

        Booking booking = new Booking(provider, customer, req.start(), end);
        try {
            // saveAndFlush forces the INSERT now, so the unique constraint
            // (provider_id, start_time) rejects a concurrent duplicate here.
            return bookings.saveAndFlush(booking);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "slot already booked");
        }
    }

    public List<Booking> myBookings(String customerEmail) {
        User customer = users.findByEmail(customerEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unknown user"));
        return bookings.findByCustomer(customer);
    }

    @Transactional
    public void cancel(String email, Long bookingId) {
        Booking booking = bookings.findById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found"));
        boolean isOwner = booking.getCustomer().getEmail().equals(email)
            || booking.getProvider().getEmail().equals(email);
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your booking");
        }
        // Delete frees the slot for rebooking (the unique constraint ignores status).
        bookings.delete(booking);
    }

    /**
     * If {@code start} aligns with one of the provider's availability slots,
     * return that slot's end instant. Otherwise empty.
     */
    private Optional<Instant> matchingSlotEnd(User provider, Instant start) {
        for (AvailabilityRule rule : rules.findByProvider(provider)) {
            ZoneId zone = ZoneId.of(rule.getZoneId());
            ZonedDateTime local = start.atZone(zone);

            if (local.getDayOfWeek() != rule.getDayOfWeek()) {
                continue;
            }
            LocalTime lt = local.toLocalTime();
            if (lt.isBefore(rule.getStartTime()) || !lt.isBefore(rule.getEndTime())) {
                continue;
            }
            long fromStart = Duration.between(rule.getStartTime(), lt).toMinutes();
            if (fromStart % rule.getSlotMinutes() != 0) {
                continue; // not aligned to a slot boundary
            }
            if (lt.plusMinutes(rule.getSlotMinutes()).isAfter(rule.getEndTime())) {
                continue; // slot would run past availability
            }
            return Optional.of(start.plus(Duration.ofMinutes(rule.getSlotMinutes())));
        }
        return Optional.empty();
    }
}
