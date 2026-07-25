package net.akive.api.plan.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.port.out.ValidateTotalQuantityPort;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import org.springframework.stereotype.Component;

import static net.akive.api.common.exception.business.BadRequestError.TOTAL_QUANTITY_LIMIT_EXCEEDED;
import static net.akive.api.common.exception.business.ConflictError.SUBJECT_NAME_ALREADY_EXISTS;

@Component
@RequiredArgsConstructor
class TotalQuantityValidator {

    private static final int MAX_TOTAL_QUANTITY_COUNT_PER_SESSIONAL_PLAN = 50;

    private final ValidateTotalQuantityPort validateTotalQuantityPort;

    public void validateCreatable(Long sessionalPlanId, String subjectName) {
        if (validateTotalQuantityPort.existsBySessionalPlanIdAndSubjectName(sessionalPlanId, subjectName)) {
            throw new ConflictException(SUBJECT_NAME_ALREADY_EXISTS);
        }

        int totalQuantityCount = validateTotalQuantityPort.countBySessionalPlanId(sessionalPlanId);
        if (totalQuantityCount >= MAX_TOTAL_QUANTITY_COUNT_PER_SESSIONAL_PLAN) {
            throw new BadRequestException(TOTAL_QUANTITY_LIMIT_EXCEEDED);
        }
    }
}