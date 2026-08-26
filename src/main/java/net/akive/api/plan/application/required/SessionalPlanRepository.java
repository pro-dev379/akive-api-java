package net.akive.api.plan.application.required;

import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.value.SessionPeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SessionalPlanRepository {

    SessionalPlan save(SessionalPlan sessionalPlan);

    Optional<SessionalPlan> findBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId);

    Optional<SessionPeriod> findSessionPeriodBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId);

    List<SessionalPlan> findAllByUserIdOrderBySessionalPlanIdDesc(Long userId);

    boolean existsBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId);

    boolean existsByUserIdAndSessionName(Long userId, String sessionName);

    boolean existsByUserIdAndSessionNameAndSessionalPlanIdNot(
            Long userId,
            String sessionName,
            Long sessionalPlanId
    );

    void deleteBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId);

    long countInProgressByUserId(Long userId, LocalDate now);
}
