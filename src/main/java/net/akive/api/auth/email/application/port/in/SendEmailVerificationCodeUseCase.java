package net.akive.api.auth.email.application.port.in;

import net.akive.api.auth.email.application.dto.EmailVerificationCodeSendResult;
import net.akive.api.auth.email.domain.EmailVerificationPurpose;

public interface SendEmailVerificationCodeUseCase {

    EmailVerificationCodeSendResult send(String email, EmailVerificationPurpose verificationPurpose);
}
