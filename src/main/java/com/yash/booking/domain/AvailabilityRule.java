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
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * A provider's recurring weekly availability, e.g. "Mondays 09:00–17:00 in
 * 30-minute slots". Times are stored in the provider's zone (see zoneId) and
 * converted to UTC when generating concrete slots.
 */
@Entity
@Table(name = "availability_rules")
public class AvailabilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id")
    private User provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    // IANA zone the start/end times are expressed in, e.g. "Asia/Kolkata".
    @Column(nullable = false)
    private String zoneId;

    @Column(nullable = false)
    private int slotMinutes;

    protected AvailabilityRule() {
    }

    public AvailabilityRule(User provider, DayOfWeek dayOfWeek, LocalTime startTime,
                            LocalTime endTime, String zoneId, int slotMinutes) {
        this.provider = provider;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.zoneId = zoneId;
        this.slotMinutes = slotMinutes;
    }

    public Long getId() {
        return id;
    }

    public User getProvider() {
        return provider;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getZoneId() {
        return zoneId;
    }

    public int getSlotMinutes() {
        return slotMinutes;
    }
}
