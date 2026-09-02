package net.akive.api.plan.application.service.totalquantity;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityCreator;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityModifier;
import net.akive.api.plan.application.provided.totalquantity.dto.*;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.domain.model.TotalQuantity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.NotFoundError.TOTAL_QUANTITY_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class TotalQuantityCommandService implements TotalQuantityCreator, TotalQuantityModifier {

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
    public void update(TotalQuantityUpdateCommand command)
            throws BadRequestException, ConflictException, NotFoundException {
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
    public void delete(TotalQuantityDeleteCommand command) throws NotFoundException {
        validateSessionalPlan(command.sessionalPlanId(), command.userId());

        totalQuantityRepository.deleteByTotalQuantityIdAndSessionalPlanId(
                command.totalQuantityId(),
                command.sessionalPlanId()
        );
    }

    @Override
    public void updateWeeklyQuantityAmount(WeeklyQuantityAmountUpdateCommand command)
            throws BadRequestException, ConflictException, NotFoundException {
        validateSessionalPlan(command.sessionalPlanId(), command.userId());

        TotalQuantity totalQuantity = totalQuantityRepository
                .findBySessionalPlanIdAndTotalQuantityId(command.sessionalPlanId(), command.totalQuantityId())
                .orElseThrow(() -> new NotFoundException(TOTAL_QUANTITY_NOT_FOUND));

        totalQuantity.updateWeeklyQuantityAmount(command.weeklyQuantityId(), command.targetWeeklyQuantityAmount());

        totalQuantityRepository.save(totalQuantity);
    }

    @Override
    public void delayWeeklyAmountTo(WeeklyAmountDelayCommand command)
            throws BadRequestException, NotFoundException {
        validateSessionalPlan(command.sessionalPlanId(), command.userId());

        TotalQuantity totalQuantity = totalQuantityRepository
                .findBySessionalPlanIdAndTotalQuantityId(command.sessionalPlanId(), command.totalQuantityId())
                .orElseThrow(() -> new NotFoundException(TOTAL_QUANTITY_NOT_FOUND));

        totalQuantity.delayWeeklyAmountTo(
                command.weeklyQuantityId(),
                command.targetWeeklyQuantityId(),
                command.weeklyAmountToDelay()
        );

        totalQuantityRepository.save(totalQuantity);
    }

    @Override
    public void updateDailyAmount(DailyAmountUpdateCommand command)
            throws BadRequestException, ConflictException, NotFoundException {
        validateSessionalPlan(command.sessionalPlanId(), command.userId());

        TotalQuantity totalQuantity = totalQuantityRepository
                .findBySessionalPlanIdAndTotalQuantityId(command.sessionalPlanId(), command.totalQuantityId())
                .orElseThrow(() -> new NotFoundException(TOTAL_QUANTITY_NOT_FOUND));

        totalQuantity.updateDailyAmount(
                command.weeklyQuantityId(),
                command.dailyQuantityId(),
                command.targetDailyAmount(),
                command.targetDailyMemo()
        );

        totalQuantityRepository.save(totalQuantity);
    }

    private void validateSessionalPlan(Long sessionalPlanId, Long userId) {
        boolean hasSessionalPlan = sessionalPlanRepository
                .existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);

        if (!hasSessionalPlan) {
            throw new NotFoundException(SESSIONAL_PLAN_NOT_FOUND);
        }
    }
}
