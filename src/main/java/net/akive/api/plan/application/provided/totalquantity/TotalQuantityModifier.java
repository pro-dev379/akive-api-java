package net.akive.api.plan.application.provided.totalquantity;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.*;

public interface TotalQuantityModifier {

    void update(TotalQuantityUpdateCommand command)
            throws BusinessRuleViolationException, ResourceConflictException, ResourceNotFoundException;

    void delete(TotalQuantityDeleteCommand command) throws ResourceNotFoundException;

    void updateWeeklyQuantityAmount(WeeklyQuantityAmountUpdateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException;

    void delayWeeklyAmountTo(WeeklyAmountDelayCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException;

    void updateDailyAmount(DailyAmountUpdateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException;

    void updateDailyCompletedAmount(DailyCompletedAmountUpdateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException;

    void delayDailyAmountTo(DailyAmountDelayCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException;
}
