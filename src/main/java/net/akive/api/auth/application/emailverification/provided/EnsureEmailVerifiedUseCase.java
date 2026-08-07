package net.akive.api.auth.application.emailverification.provided;

import net.akive.api.auth.application.emailverification.provided.dto.EnsureEmailVerifiedCommand;

public interface EnsureEmailVerifiedUseCase {

    void ensureVerified(EnsureEmailVerifiedCommand command);
}
