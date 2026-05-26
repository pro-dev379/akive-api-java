package net.akive.api.auth.application.port.in;

import net.akive.api.auth.domain.EmailVerificationPurpose;

public interface VerifyEmailVerificationCodeUseCase {

    void verify(String email, EmailVerificationPurpose verificationPurpose, String verificationCode);
}
