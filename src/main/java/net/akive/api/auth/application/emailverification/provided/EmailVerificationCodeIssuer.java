package net.akive.api.auth.application.emailverification.provided;

import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationIssueCommand;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.common.exception.business.TooManyRequestsException;

public interface EmailVerificationCodeIssuer {

    EmailVerification issue(EmailVerificationIssueCommand command)
            throws ConflictException, NotFoundException, TooManyRequestsException;
}
