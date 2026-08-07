package net.akive.api.plan.application.required;

import java.time.LocalDate;

public interface ValidateSessionalPlanPort {

    boolean existsByUserIdAndSessionName(Long userId, String sessionName);
    boolean existsByUserIdAndSessionNameAndSessionalPlanIdNot(Long userId, String sessionName, Long sessionalPlanId);
    int countInProgressByUserId(Long userId, LocalDate now);
}
