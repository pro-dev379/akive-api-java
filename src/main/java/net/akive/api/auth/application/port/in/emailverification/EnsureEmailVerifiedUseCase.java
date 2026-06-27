package net.akive.api.auth.application.port.in.emailverification;

import net.akive.api.auth.application.dto.command.emailverification.EmailVerificationEnsureVerifiedCommand;

public interface EnsureEmailVerifiedUseCase {

    void ensureVerified(EmailVerificationEnsureVerifiedCommand command);
}
