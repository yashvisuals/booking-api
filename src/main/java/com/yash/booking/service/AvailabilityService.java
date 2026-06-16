package com.yash.booking.service;

import com.yash.booking.domain.AvailabilityRule;
import com.yash.booking.domain.Booking;
import com.yash.booking.domain.BookingStatus;
import com.yash.booking.domain.User;
import com.yash.booking.repo.AvailabilityRuleRepository;
import com.yash.booking.repo.BookingRepository;
import com.yash.booking.repo.UserRepository;
import com.yash.booking.web.dto.CreateAvailabilityRequest;
import com.yash.booking.web.dto.SlotResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AvailabilityService {

    private final AvailabilityRuleRepository rules;
    private final BookingRepository bookings;
    private final UserRepository users;

    public AvailabilityService(AvailabilityRuleRepository rules,
                               BookingRepository bookings, UserRepository users) {
        this.rules = rules;
        this.bookings = bookings;
        this.users = users;
    }

    public AvailabilityRule create(String email, CreateAvailabilityRequest req) {
        User provider = providerByEmail(email);

        if (!req.endTime().isAfter(req.startTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "endTime must be after startTime");
        }
        try {
            ZoneId.of(req.zoneId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "invalid zoneId: " + req.zoneId());
        }

        AvailabilityRule rule = new AvailabilityRule(provider, req.dayOfWeek(),
            req.startTime(), req.endTime(), req.zoneId(), req.slotMinutes());
        return rules.save(rule);
    }

    public List<AvailabilityRule> list(String email) {
        return rules.findByProvider(providerByEmail(email));
    }

    public void delete(String email, Long id) {
        AvailabilityRule rule = rules.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "rule not found"));
        if (!rule.getProvider().getId().equals(providerByEmail(email).getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your rule");
        }
        rules.delete(rule);
    }

    /**
     * Generate the free slots for a provider on a given date.
     * Slot times come from the rule expressed in the provider's zone, converted
     * to UTC instants; slots already booked (CONFIRMED) are removed.
     */
    public List<SlotResponse> freeSlots(Long providerId, LocalDate date) {
        User provider = users.findById(providerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "provider not found"));

        List<SlotResponse> slots = new ArrayList<>();
        for (AvailabilityRule rule : rules.findByProviderAndDayOfWeek(provider, date.getDayOfWeek())) {
            ZoneId zone = ZoneId.of(rule.getZoneId());
            LocalTime t = rule.getStartTime();
            while (!t.plusMinutes(rule.getSlotMinutes()).isAfter(rule.getEndTime())) {
                Instant start = date.atTime(t).atZone(zone).toInstant();
                Instant end = date.atTime(t.plusMinutes(rule.getSlotMinutes())).atZone(zone).toInstant();
                slots.add(new SlotResponse(start, end));
                t = t.plusMinutes(rule.getSlotMinutes());
            }
        }

        if (slots.isEmpty()) {
            return slots;
        }

        // Remove slots that already have a confirmed booking.
        Instant windowStart = slots.stream().map(SlotResponse::start).min(Instant::compareTo).get();
        Instant windowEnd = slots.stream().map(SlotResponse::end).max(Instant::compareTo).get();
        Set<Instant> taken = bookings
            .findByProviderAndStartTimeBetween(provider, windowStart, windowEnd).stream()
            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
            .map(Booking::getStartTime)
            .collect(Collectors.toSet());

        return slots.stream()
            .filter(s -> !taken.contains(s.start()))
            .sorted((a, b) -> a.start().compareTo(b.start()))
            .toList();
    }

    private User providerByEmail(String email) {
        return users.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unknown user"));
    }
}
