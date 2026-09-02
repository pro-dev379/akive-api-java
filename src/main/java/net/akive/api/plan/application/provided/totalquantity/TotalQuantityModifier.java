package net.akive.api.plan.application.provided.totalquantity;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.*;

public interface TotalQuantityModifier {

    void update(TotalQuantityUpdateCommand command) throws BadRequestException, ConflictException, NotFoundException;

    void delete(TotalQuantityDeleteCommand command) throws NotFoundException;

    void updateWeeklyQuantityAmount(WeeklyQuantityAmountUpdateCommand command)
            throws BadRequestException, ConflictException, NotFoundException;

    void delayWeeklyAmountTo(WeeklyAmountDelayCommand command) throws BadRequestException, NotFoundException;

    void updateDailyAmount(DailyAmountUpdateCommand command)
            throws BadRequestException, ConflictException, NotFoundException;
}
