package net.akive.api.auth.application.port.in.emailverification;

import net.akive.api.auth.application.dto.command.emailverification.EnsureEmailVerifiedCommand;

public interface EnsureEmailVerifiedUseCase {

    void ensureVerified(EnsureEmailVerifiedCommand command);
}
