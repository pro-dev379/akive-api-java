package net.akive.api.auth.application.port.in.emailverification;

import net.akive.api.auth.application.dto.EmailVerificationCodeVerifyCommand;

public interface VerifyEmailVerificationCodeUseCase {

    void verify(EmailVerificationCodeVerifyCommand command);
}
