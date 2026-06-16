package com.yash.booking.web;

import com.yash.booking.domain.Role;
import com.yash.booking.repo.UserRepository;
import com.yash.booking.service.AvailabilityService;
import com.yash.booking.web.dto.ProviderResponse;
import com.yash.booking.web.dto.SlotResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/providers")
public class SlotController {

    private final AvailabilityService availability;
    private final UserRepository users;

    public SlotController(AvailabilityService availability, UserRepository users) {
        this.availability = availability;
        this.users = users;
    }

    // List all providers so customers can pick one.
    @GetMapping
    public List<ProviderResponse> providers() {
        return users.findByRole(Role.PROVIDER).stream()
            .map(u -> new ProviderResponse(u.getId(), u.getEmail()))
            .toList();
    }

    // Any authenticated user (e.g. a customer) can view a provider's free slots.
    @GetMapping("/{providerId}/slots")
    public List<SlotResponse> slots(
        @PathVariable Long providerId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return availability.freeSlots(providerId, date);
    }
}
