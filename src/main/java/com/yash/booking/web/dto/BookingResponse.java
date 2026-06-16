package com.yash.booking.web.dto;

import com.yash.booking.domain.Booking;
import com.yash.booking.domain.BookingStatus;
import java.time.Instant;

public record BookingResponse(
    Long id,
    Long providerId,
    String providerEmail,
    String customerEmail,
    Instant start,
    Instant end,
    BookingStatus status) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(
            b.getId(),
            b.getProvider().getId(),
            b.getProvider().getEmail(),
            b.getCustomer().getEmail(),
            b.getStartTime(),
            b.getEndTime(),
            b.getStatus());
    }
}
