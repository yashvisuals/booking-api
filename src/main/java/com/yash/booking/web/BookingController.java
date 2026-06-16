package com.yash.booking.web;

import com.yash.booking.service.BookingService;
import com.yash.booking.web.dto.BookingResponse;
import com.yash.booking.web.dto.CreateBookingRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponse book(@Valid @RequestBody CreateBookingRequest req, Authentication auth) {
        return BookingResponse.from(bookingService.book(auth.getName(), req));
    }

    @GetMapping("/me")
    public List<BookingResponse> myBookings(Authentication auth) {
        return bookingService.myBookings(auth.getName()).stream()
            .map(BookingResponse::from)
            .toList();
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id, Authentication auth) {
        bookingService.cancel(auth.getName(), id);
    }
}
