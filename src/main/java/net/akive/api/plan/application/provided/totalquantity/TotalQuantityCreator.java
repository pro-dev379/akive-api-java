package net.akive.api.plan.application.provided.totalquantity;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityCreateCommand;

public interface TotalQuantityCreator {

    Long create(TotalQuantityCreateCommand command) throws BadRequestException, ConflictException, NotFoundException;
}
