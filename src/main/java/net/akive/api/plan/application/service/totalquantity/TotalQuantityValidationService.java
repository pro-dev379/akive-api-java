package net.akive.api.plan.application.service.totalquantity;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import org.springframework.stereotype.Component;

import static net.akive.api.common.exception.business.BadRequestError.TOTAL_QUANTITY_LIMIT_EXCEEDED;
import static net.akive.api.common.exception.business.ConflictError.SUBJECT_NAME_ALREADY_EXISTS;
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
            throw new ConflictException(SUBJECT_NAME_ALREADY_EXISTS);
        }

        long totalQuantityCount = totalQuantityRepository.countBySessionalPlanId(sessionalPlanId);

        if (totalQuantityCount >= TOTAL_QUANTITY_MAX_COUNT) {
            throw new BadRequestException(TOTAL_QUANTITY_LIMIT_EXCEEDED);
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
            throw new ConflictException(SUBJECT_NAME_ALREADY_EXISTS);
        }
    }
}
