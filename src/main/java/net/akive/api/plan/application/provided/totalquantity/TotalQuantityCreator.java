package net.akive.api.plan.application.provided.totalquantity;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityCreateCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityDeleteCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityUpdateCommand;

public interface TotalQuantityCreator {

    Long create(TotalQuantityCreateCommand command) throws BadRequestException, ConflictException, NotFoundException;

    void update(TotalQuantityUpdateCommand command) throws BadRequestException, ConflictException, NotFoundException;

    void delete(TotalQuantityDeleteCommand command) throws NotFoundException;
}
