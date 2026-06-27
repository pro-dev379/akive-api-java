package net.akive.api.auth.application.port.in.emailverification;

import net.akive.api.auth.application.dto.command.emailverification.EmailVerificationCodeVerifyCommand;

public interface VerifyEmailVerificationCodeUseCase {

    void verify(EmailVerificationCodeVerifyCommand command);
}
