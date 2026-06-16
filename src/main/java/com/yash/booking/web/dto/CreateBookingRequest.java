package com.yash.booking.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateBookingRequest(
    @NotNull Long providerId,
    @NotNull Instant start) {
}
