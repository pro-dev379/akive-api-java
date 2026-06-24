package net.akive.api.auth.application.port.in.emailverification;

import net.akive.api.auth.application.dto.EmailVerificationEnsureVerifiedCommand;

public interface EnsureEmailVerifiedUseCase {

    void ensureVerified(EmailVerificationEnsureVerifiedCommand command);
}
