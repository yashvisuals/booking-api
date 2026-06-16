package com.yash.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.yash.booking.domain.AvailabilityRule;
import com.yash.booking.domain.Role;
import com.yash.booking.domain.User;
import com.yash.booking.repo.AvailabilityRuleRepository;
import com.yash.booking.repo.BookingRepository;
import com.yash.booking.repo.UserRepository;
import com.yash.booking.service.BookingService;
import com.yash.booking.web.dto.CreateBookingRequest;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
class BookingConcurrencyTest {

    @Autowired
    BookingService bookingService;
    @Autowired
    UserRepository users;
    @Autowired
    AvailabilityRuleRepository rules;
    @Autowired
    BookingRepository bookings;

    private Long providerId;
    // 09:00 IST Tuesday == 03:30 UTC
    private static final Instant SLOT = Instant.parse("2026-06-23T03:30:00Z");

    @BeforeEach
    void setUp() {
        bookings.deleteAll();
        rules.deleteAll();
        users.deleteAll();

        User provider = users.save(new User("prov@test.com", "pw", Role.PROVIDER));
        users.save(new User("cust@test.com", "pw", Role.CUSTOMER));
        rules.save(new AvailabilityRule(provider, DayOfWeek.TUESDAY,
            LocalTime.of(9, 0), LocalTime.of(12, 0), "Asia/Kolkata", 30));
        providerId = provider.getId();
    }

    @Test
    void bookingTheSameSlotTwiceIsRejected() {
        CreateBookingRequest req = new CreateBookingRequest(providerId, SLOT);
        bookingService.book("cust@test.com", req);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> bookingService.book("cust@test.com", req));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void twoConcurrentBookingsOnlyOneWins() throws InterruptedException {
        CreateBookingRequest req = new CreateBookingRequest(providerId, SLOT);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        Runnable attempt = () -> {
            ready.countDown();
            try {
                go.await();               // release both threads together
                bookingService.book("cust@test.com", req);
                success.incrementAndGet();
            } catch (ResponseStatusException e) {
                if (e.getStatusCode() == HttpStatus.CONFLICT) {
                    conflict.incrementAndGet();
                }
            } catch (Exception ignored) {
                // any other failure is also "not a success"
            } finally {
                done.countDown();
            }
        };

        pool.submit(attempt);
        pool.submit(attempt);
        ready.await();   // both threads positioned
        go.countDown();  // fire simultaneously
        done.await();
        pool.shutdown();

        assertEquals(1, success.get(), "exactly one booking should succeed");
        assertEquals(1, conflict.get(), "the other should get a 409 conflict");
        assertEquals(1, bookings.count(), "only one row should exist");
    }
}
