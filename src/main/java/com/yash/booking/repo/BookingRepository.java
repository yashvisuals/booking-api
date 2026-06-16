package com.yash.booking.repo;

import com.yash.booking.domain.Booking;
import com.yash.booking.domain.User;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // All bookings for a provider that start within a window — used to subtract
    // already-taken slots when generating availability for a day.
    List<Booking> findByProviderAndStartTimeBetween(User provider, Instant from, Instant to);

    List<Booking> findByCustomer(User customer);
}
