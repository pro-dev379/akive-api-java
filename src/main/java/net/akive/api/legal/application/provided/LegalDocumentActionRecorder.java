package net.akive.api.legal.application.provided;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.legal.application.provided.dto.LegalDocumentActionsRecordCommand;

public interface LegalDocumentActionRecorder {

    void record(LegalDocumentActionsRecordCommand command)
            throws BusinessRuleViolationException, ResourceConflictException, ResourceNotFoundException;
}
