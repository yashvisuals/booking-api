package com.yash.booking.repo;

import com.yash.booking.domain.AvailabilityRule;
import com.yash.booking.domain.User;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityRuleRepository extends JpaRepository<AvailabilityRule, Long> {
    List<AvailabilityRule> findByProvider(User provider);

    List<AvailabilityRule> findByProviderAndDayOfWeek(User provider, DayOfWeek dayOfWeek);
}
