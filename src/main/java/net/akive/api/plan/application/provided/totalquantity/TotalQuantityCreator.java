package net.akive.api.plan.application.provided.totalquantity;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityCreateCommand;

public interface TotalQuantityCreator {

    Long create(TotalQuantityCreateCommand command)
            throws BusinessRuleViolationException, ResourceConflictException, ResourceNotFoundException;
}
