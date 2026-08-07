package net.akive.api.plan.application.required;

public interface DeleteTotalQuantityPort {

    long deleteByTotalQuantityIdAndSessionalPlanIdAndUserId(Long totalQuantityId, Long sessionalPlanId, Long userId);
}
