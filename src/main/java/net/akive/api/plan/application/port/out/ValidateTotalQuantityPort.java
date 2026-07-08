package net.akive.api.plan.application.port.out;

public interface ValidateTotalQuantityPort {

    boolean existsBySessionalPlanId(Long sessionalPlanId);
}