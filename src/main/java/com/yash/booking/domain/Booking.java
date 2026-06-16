package com.yash.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;

/**
 * A reserved time slot. start/end are stored as UTC instants.
 *
 * The unique constraint on (provider_id, start_time) is the core of conflict
 * detection: the database itself refuses a second booking for the same provider
 * at the same instant, so two concurrent requests can never both succeed.
 */
@Entity
@Table(
    name = "bookings",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_provider_start",
        columnNames = {"provider_id", "start_time"}
    )
)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id")
    private User provider;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private User customer;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    // Optimistic locking guard for updates (e.g. cancellations).
    @Version
    private Long version;

    protected Booking() {
    }

    public Booking(User provider, User customer, Instant startTime, Instant endTime) {
        this.provider = provider;
        this.customer = customer;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = BookingStatus.CONFIRMED;
    }

    public Long getId() {
        return id;
    }

    public User getProvider() {
        return provider;
    }

    public User getCustomer() {
        return customer;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
