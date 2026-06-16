package com.yash.booking.web.dto;

import com.yash.booking.domain.AvailabilityRule;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityResponse(
    Long id,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    String zoneId,
    int slotMinutes) {

    public static AvailabilityResponse from(AvailabilityRule rule) {
        return new AvailabilityResponse(
            rule.getId(),
            rule.getDayOfWeek(),
            rule.getStartTime(),
            rule.getEndTime(),
            rule.getZoneId(),
            rule.getSlotMinutes());
    }
}
