package net.akive.api.user.application.required;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.required.data.SignUpLegalDocumentActionData;

import java.util.List;

public interface SignUpLegalDocumentActionRecorder {

    void record(Long userId, List<SignUpLegalDocumentActionData> legalDocumentActions)
            throws BusinessRuleViolationException, ResourceConflictException, ResourceNotFoundException;
}
