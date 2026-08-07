package net.akive.api.plan.application.required;

import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionalPlan;

import java.util.List;
import java.util.Optional;

public interface LoadSessionalPlanPort {

    Optional<SessionalPlan> loadBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId);
    Optional<SessionPeriod> loadSessionPeriodBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId);
    List<SessionalPlan> loadAllByUserIdOrderBySessionalPlanIdDesc(Long userId);
}
