package net.akive.api.plan.application.service.totalquantity;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityCreator;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityCreateCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityDeleteCommand;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.domain.model.TotalQuantity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class TotalQuantityCommandService implements TotalQuantityCreator {

    private final SessionalPlanRepository sessionalPlanRepository;
    private final TotalQuantityRepository totalQuantityRepository;
    private final TotalQuantityDomainService totalQuantityDomainService;

    @Override
    public Long create(TotalQuantityCreateCommand command)
            throws BadRequestException, ConflictException, NotFoundException {
        TotalQuantity totalQuantity = totalQuantityDomainService.create(
                command.sessionalPlanId(),
                command.userId(),
                command.subject(),
                command.totalQuantityAmount()
        );

        TotalQuantity savedTotalQuantity = totalQuantityRepository.save(totalQuantity);

        return savedTotalQuantity.getTotalQuantityId();
    }

    @Override
    public void delete(TotalQuantityDeleteCommand command) throws NotFoundException {
        validateSessionalPlan(command.sessionalPlanId(), command.userId());

        totalQuantityRepository.deleteByTotalQuantityIdAndSessionalPlanId(
                command.totalQuantityId(),
                command.sessionalPlanId()
        );
    }

    private void validateSessionalPlan(Long sessionalPlanId, Long userId) {
        boolean hasSessionalPlan = sessionalPlanRepository
                .existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);

        if (!hasSessionalPlan) {
            throw new NotFoundException(SESSIONAL_PLAN_NOT_FOUND);
        }
    }
}
