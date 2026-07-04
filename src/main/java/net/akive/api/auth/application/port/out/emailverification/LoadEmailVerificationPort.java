package net.akive.api.auth.application.port.out.emailverification;

import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

import java.util.Optional;

public interface LoadEmailVerificationPort {

    Optional<EmailVerification> loadByEmailAndVerificationPurpose(String email, EmailVerificationPurpose verificationPurpose);
}
