package net.akive.api.user.application.required;

import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.BusinessRuleViolationException;

public interface UserEmailVerificationChecker {

    void ensureEmailVerifiedForSignUp(Email email) throws BusinessRuleViolationException;

    void ensureEmailVerifiedForPasswordReset(Email email) throws BusinessRuleViolationException;
}
