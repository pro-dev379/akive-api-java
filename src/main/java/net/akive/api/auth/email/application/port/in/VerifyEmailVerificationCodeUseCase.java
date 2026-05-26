package net.akive.api.auth.email.application.port.in;

import net.akive.api.auth.email.domain.EmailVerificationPurpose;

public interface VerifyEmailVerificationCodeUseCase {

    void verify(String email, EmailVerificationPurpose verificationPurpose, String verificationCode);
}
