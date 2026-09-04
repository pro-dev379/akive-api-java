package net.akive.api.auth.application.emailverification.provided;

import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationIssueCommand;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.exception.business.RateLimitExceededException;

public interface EmailVerificationCodeIssuer {

    EmailVerification issue(EmailVerificationIssueCommand command)
            throws ResourceConflictException, ResourceNotFoundException, RateLimitExceededException;
}
