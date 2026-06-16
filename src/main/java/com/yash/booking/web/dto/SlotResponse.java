package com.yash.booking.web.dto;

import java.time.Instant;

/** A bookable slot as UTC instants; the frontend renders them in local time. */
public record SlotResponse(Instant start, Instant end) {
}
