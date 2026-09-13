package net.akive.api.dailylog.application.provided;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;

public interface DailyLogCreator {

    Long create(DailyLogCreateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException, ResourceConflictException;
}
