package net.akive.api.plan.application.required;

public interface DeleteSessionalPlanPort {

    long deleteBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId);
}
