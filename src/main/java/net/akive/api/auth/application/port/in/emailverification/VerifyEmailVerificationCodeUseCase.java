package net.akive.api.auth.application.port.in.emailverification;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

public interface VerifyEmailVerificationCodeUseCase {

    void verify(String email, EmailVerificationPurpose verificationPurpose, String verificationCode);
}
