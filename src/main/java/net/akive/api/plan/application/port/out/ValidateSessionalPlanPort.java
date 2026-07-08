package net.akive.api.plan.application.port.out;

public interface ValidateSessionalPlanPort {

    boolean existsByUserIdAndSessionName(Long userId, String sessionName);
    boolean existsByUserIdAndSessionNameAndSessionalPlanIdNot(Long userId, String sessionName, Long sessionalPlanId);
}
