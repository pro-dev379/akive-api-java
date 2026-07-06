package net.akive.api.plan.application.port.out;

public interface ValidateSessionalPlanPort {

    boolean existsByUserIdAndSessionName(Long userId, String sessionName);
}
