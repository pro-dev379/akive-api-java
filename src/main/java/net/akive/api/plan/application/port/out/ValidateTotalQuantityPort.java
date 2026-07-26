package net.akive.api.plan.application.port.out;

public interface ValidateTotalQuantityPort {

    boolean existsBySessionalPlanId(Long sessionalPlanId);
    boolean existsBySessionalPlanIdAndSubjectName(Long sessionalPlanId, String subjectName);
    int countBySessionalPlanId(Long sessionalPlanId);
}
