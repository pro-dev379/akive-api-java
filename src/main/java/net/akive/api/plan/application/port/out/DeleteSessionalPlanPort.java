package net.akive.api.plan.application.port.out;

public interface DeleteSessionalPlanPort {

    long deleteBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId);
}
