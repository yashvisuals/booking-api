package com.yash.booking.web;

import com.yash.booking.service.AvailabilityService;
import com.yash.booking.web.dto.AvailabilityResponse;
import com.yash.booking.web.dto.CreateAvailabilityRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/availability")
@PreAuthorize("hasRole('PROVIDER')")
public class AvailabilityController {

    private final AvailabilityService availability;

    public AvailabilityController(AvailabilityService availability) {
        this.availability = availability;
    }

    @PostMapping
    public AvailabilityResponse create(@Valid @RequestBody CreateAvailabilityRequest req,
                                       Authentication auth) {
        return AvailabilityResponse.from(availability.create(auth.getName(), req));
    }

    @GetMapping
    public List<AvailabilityResponse> list(Authentication auth) {
        return availability.list(auth.getName()).stream()
            .map(AvailabilityResponse::from)
            .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        availability.delete(auth.getName(), id);
    }
}
