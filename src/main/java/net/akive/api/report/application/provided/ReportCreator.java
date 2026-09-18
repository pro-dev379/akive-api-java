package net.akive.api.report.application.provided;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.report.application.provided.dto.ReportCreateCommand;
import net.akive.api.report.application.provided.dto.ReportFeedbackItemsUpdateCommand;

public interface ReportCreator {

    Long create(ReportCreateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException, ResourceConflictException;

    void delete(Long reportId, Long userId);

    void updateFeedbackItems(ReportFeedbackItemsUpdateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException;
}
