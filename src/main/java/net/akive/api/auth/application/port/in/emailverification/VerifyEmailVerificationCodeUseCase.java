package net.akive.api.auth.application.port.in.emailverification;

import net.akive.api.auth.application.dto.command.emailverification.VerifyEmailVerificationCodeCommand;

public interface VerifyEmailVerificationCodeUseCase {

    void verify(VerifyEmailVerificationCodeCommand command);
}
