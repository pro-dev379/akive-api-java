package net.akive.api.auth.application.emailverification.provided;

import net.akive.api.auth.application.emailverification.provided.dto.VerifyEmailVerificationCodeCommand;

public interface VerifyEmailVerificationCodeUseCase {

    void verify(VerifyEmailVerificationCodeCommand command);
}
