package net.akive.api.auth.application.emailverification.provided;

import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationCodeVerifyCommand;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;

public interface EmailVerificationCodeVerifier {

    void verify(EmailVerificationCodeVerifyCommand command)
            throws BusinessRuleViolationException,
            ResourceNotFoundException,
            AuthenticationException,
            ResourceConflictException;
}
