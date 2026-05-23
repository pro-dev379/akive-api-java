package net.akive.api.auth.email.application.port.in;

import net.akive.api.auth.email.domain.EmailAuthPurpose;

public interface VerifyEmailAuthCodeUseCase {
    void verifyAuthCode(String email, EmailAuthPurpose authPurpose, String authCode);
}
