package net.akive.api.plan.application.port.out;

public interface DeleteTotalQuantityPort {

    long deleteByTotalQuantityIdAndSessionalPlanIdAndUserId(Long totalQuantityId, Long sessionalPlanId, Long userId);
}
