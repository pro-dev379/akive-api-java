package net.akive.api.auth.application.emailverification.provided;

import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationEnsureCommand;
import net.akive.api.common.exception.business.BusinessRuleViolationException;

public interface EmailVerificationChecker {

    void ensureEmailVerified(EmailVerificationEnsureCommand command) throws BusinessRuleViolationException;
}
