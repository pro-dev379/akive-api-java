package net.akive.api.plan.application.service.totalquantity;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityCreator;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityModifier;
import net.akive.api.plan.application.provided.totalquantity.dto.*;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.domain.model.TotalQuantity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.TOTAL_QUANTITY_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class TotalQuantityCommandService implements TotalQuantityCreator, TotalQuantityModifier {

    private final TotalQuantityRepository totalQuantityRepository;
    private final TotalQuantityDomainService totalQuantityDomainService;

    @Override
    public Long create(TotalQuantityCreateCommand command)
            throws BusinessRuleViolationException, ResourceConflictException, ResourceNotFoundException {
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
    public void update(TotalQuantityUpdateCommand command)
            throws BusinessRuleViolationException, ResourceConflictException, ResourceNotFoundException {
        TotalQuantity updatedTotalQuantity = totalQuantityDomainService.update(
                command.totalQuantityId(),
                command.sessionalPlanId(),
                command.userId(),
                command.targetSubject(),
                command.targetTotalQuantityAmount()
        );

        totalQuantityRepository.save(updatedTotalQuantity);
    }

    @Override
    public void delete(TotalQuantityDeleteCommand command) throws ResourceNotFoundException {
        TotalQuantity totalQuantity = findTotalQuantityOrThrow(
                command.sessionalPlanId(),
                command.userId(),
                command.totalQuantityId()
        );

        totalQuantityRepository.delete(totalQuantity);
    }

    @Override
    public void updateWeeklyQuantityAmount(WeeklyQuantityAmountUpdateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException {
        TotalQuantity totalQuantity = findTotalQuantityOrThrow(
                command.sessionalPlanId(),
                command.userId(),
                command.totalQuantityId()
        );

        totalQuantity.updateWeeklyQuantityAmount(command.weeklyQuantityId(), command.targetWeeklyQuantityAmount());

        totalQuantityRepository.save(totalQuantity);
    }

    @Override
    public void delayWeeklyAmountTo(WeeklyAmountDelayCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException {
        TotalQuantity totalQuantity = findTotalQuantityOrThrow(
                command.sessionalPlanId(),
                command.userId(),
                command.totalQuantityId()
        );

        totalQuantity.delayWeeklyAmountTo(
                command.weeklyQuantityId(),
                command.receivingWeeklyQuantityId(),
                command.weeklyAmountToDelay()
        );

        totalQuantityRepository.save(totalQuantity);
    }

    @Override
    public void updateDailyAmount(DailyAmountUpdateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException {
        TotalQuantity totalQuantity = findTotalQuantityOrThrow(
                command.sessionalPlanId(),
                command.userId(),
                command.totalQuantityId()
        );

        totalQuantity.updateDailyAmount(
                command.weeklyQuantityId(),
                command.dailyQuantityId(),
                command.targetDailyAmount(),
                command.targetDailyMemo()
        );

        totalQuantityRepository.save(totalQuantity);
    }

    @Override
    public void updateDailyCompletedAmount(DailyCompletedAmountUpdateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException {
        TotalQuantity totalQuantity = findTotalQuantityOrThrow(
                command.sessionalPlanId(),
                command.userId(),
                command.totalQuantityId()
        );

        totalQuantity.updateDailyCompletedAmount(
                command.weeklyQuantityId(),
                command.dailyQuantityId(),
                command.targetDailyCompletedAmount()
        );

        totalQuantityRepository.save(totalQuantity);
    }

    @Override
    public void delayDailyAmountTo(DailyAmountDelayCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException {
        TotalQuantity totalQuantity = findTotalQuantityOrThrow(
                command.sessionalPlanId(),
                command.userId(),
                command.totalQuantityId()
        );

        totalQuantity.delayDailyAmountTo(
                command.weeklyQuantityId(),
                command.dailyQuantityId(),
                command.receivingDailyQuantityId(),
                command.dailyAmountToDelay()
        );

        totalQuantityRepository.save(totalQuantity);
    }

    private TotalQuantity findTotalQuantityOrThrow(Long sessionalPlanId, Long userId, Long totalQuantityId) {
        return totalQuantityRepository
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(sessionalPlanId, userId, totalQuantityId)
                .orElseThrow(() -> new ResourceNotFoundException(TOTAL_QUANTITY_NOT_FOUND));
    }
}
