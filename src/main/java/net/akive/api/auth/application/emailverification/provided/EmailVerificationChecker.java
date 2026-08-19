package net.akive.api.auth.application.emailverification.provided;

import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationEnsureCommand;

public interface EmailVerificationChecker {

    void ensureEmailVerified(EmailVerificationEnsureCommand command);
}
