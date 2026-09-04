package net.akive.api.plan.application.service.totalquantity;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import org.springframework.stereotype.Component;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.TOTAL_QUANTITY_LIMIT_EXCEEDED;
import static net.akive.api.common.exception.business.ResourceConflictError.SUBJECT_NAME_ALREADY_EXISTS;
import static net.akive.api.plan.domain.policy.PlanPolicy.TOTAL_QUANTITY_MAX_COUNT;

@Component
@RequiredArgsConstructor
class TotalQuantityValidationService {

    private final TotalQuantityRepository totalQuantityRepository;

    public void validateCreatable(Long sessionalPlanId, String subjectName) {
        boolean isDuplicated = totalQuantityRepository.existsBySessionalPlanIdAndSubjectName(
                sessionalPlanId,
                subjectName
        );

        if (isDuplicated) {
            throw new ResourceConflictException(SUBJECT_NAME_ALREADY_EXISTS);
        }

        long totalQuantityCount = totalQuantityRepository.countBySessionalPlanId(sessionalPlanId);

        if (totalQuantityCount >= TOTAL_QUANTITY_MAX_COUNT) {
            throw new BusinessRuleViolationException(TOTAL_QUANTITY_LIMIT_EXCEEDED);
        }
    }

    public void validateUpdatable(Long totalQuantityId, Long sessionalPlanId, String subjectName) {
        boolean isDuplicated = totalQuantityRepository
                .existsBySessionalPlanIdAndSubjectNameAndTotalQuantityIdNot(
                        sessionalPlanId,
                        subjectName,
                        totalQuantityId
                );

        if (isDuplicated) {
            throw new ResourceConflictException(SUBJECT_NAME_ALREADY_EXISTS);
        }
    }
}
