package net.akive.api.auth.application.emailverification.required;

import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;

import java.util.Optional;

public interface LoadEmailVerificationPort {

    Optional<EmailVerification> loadByEmailAndVerificationPurpose(Email email, EmailVerificationPurpose verificationPurpose);
}
