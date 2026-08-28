package net.akive.api.plan.application.provided.totalquantity;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityDeleteCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityUpdateCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyQuantityDelayCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyQuantityReduceCommand;

public interface TotalQuantityModifier {

    void update(TotalQuantityUpdateCommand command) throws BadRequestException, ConflictException, NotFoundException;

    void delete(TotalQuantityDeleteCommand command) throws NotFoundException;

    void reduceWeeklyQuantity(WeeklyQuantityReduceCommand command)
            throws BadRequestException, ConflictException, NotFoundException;

    void delayWeeklyQuantity(WeeklyQuantityDelayCommand command) throws BadRequestException, NotFoundException;
}
