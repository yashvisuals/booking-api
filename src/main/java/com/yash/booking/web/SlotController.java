package com.yash.booking.web;

import com.yash.booking.service.AvailabilityService;
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

    public SlotController(AvailabilityService availability) {
        this.availability = availability;
    }

    // Any authenticated user (e.g. a customer) can view a provider's free slots.
    @GetMapping("/{providerId}/slots")
    public List<SlotResponse> slots(
        @PathVariable Long providerId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return availability.freeSlots(providerId, date);
    }
}
