package net.akive.api.auth.application.port.in.emailverification;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

public interface ValidateEmailVerificationUseCase {

    void validateVerified(String email, EmailVerificationPurpose verificationPurpose);
}
