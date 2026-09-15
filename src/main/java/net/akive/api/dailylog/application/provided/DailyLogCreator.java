package net.akive.api.dailylog.application.provided;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;
import net.akive.api.dailylog.application.provided.dto.DailyLogDeleteCommand;
import net.akive.api.dailylog.application.provided.dto.DailyLogUpdateCommand;

public interface DailyLogCreator {

    Long create(DailyLogCreateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException, ResourceConflictException;

    void update(DailyLogUpdateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException, ResourceConflictException;

    void delete(DailyLogDeleteCommand command) throws ResourceNotFoundException;
}
