package net.akive.api.user.application.required;

import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.BadRequestException;

public interface UserEmailVerificationChecker {

    void ensureEmailVerifiedForSignUp(Email email) throws BadRequestException;

    void ensureEmailVerifiedForPasswordReset(Email email) throws BadRequestException;
}
