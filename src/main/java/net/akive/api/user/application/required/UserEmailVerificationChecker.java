package net.akive.api.user.application.required;

import net.akive.api.common.domain.Email;

public interface UserEmailVerificationChecker {

    void ensureEmailVerifiedForSignUp(Email email);

    void ensureEmailVerifiedForPasswordReset(Email email);
}
