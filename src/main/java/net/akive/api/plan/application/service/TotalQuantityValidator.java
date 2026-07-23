package net.akive.api.plan.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.port.out.ValidateTotalQuantityPort;
import net.akive.api.plan.exception.PlanException;
import org.springframework.stereotype.Component;

import static net.akive.api.plan.exception.PlanErrorCode.SUBJECT_NAME_ALREADY_EXISTS;
import static net.akive.api.plan.exception.PlanErrorCode.TOTAL_QUANTITY_LIMIT_EXCEEDED;

@Component
@RequiredArgsConstructor
class TotalQuantityValidator {

    private static final int MAX_TOTAL_QUANTITY_COUNT_PER_SESSIONAL_PLAN = 50;

    private final ValidateTotalQuantityPort validateTotalQuantityPort;

    public void validateCreatable(Long sessionalPlanId, String subjectName) {
        if (validateTotalQuantityPort.existsBySessionalPlanIdAndSubjectName(sessionalPlanId, subjectName)) {
            throw new PlanException(SUBJECT_NAME_ALREADY_EXISTS);
        }

        int totalQuantityCount = validateTotalQuantityPort.countBySessionalPlanId(sessionalPlanId);
        if (totalQuantityCount >= MAX_TOTAL_QUANTITY_COUNT_PER_SESSIONAL_PLAN) {
            throw new PlanException(TOTAL_QUANTITY_LIMIT_EXCEEDED);
        }
    }
}