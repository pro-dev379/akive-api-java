package net.akive.api.report.application.provided;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.report.application.provided.dto.ReportCreateCommand;

public interface ReportCreator {

    Long create(ReportCreateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException, ResourceConflictException;
}
