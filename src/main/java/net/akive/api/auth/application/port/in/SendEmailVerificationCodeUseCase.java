package net.akive.api.auth.application.port.in;

import net.akive.api.auth.application.dto.EmailVerificationCodeSendResult;
import net.akive.api.auth.domain.EmailVerificationPurpose;

public interface SendEmailVerificationCodeUseCase {

    EmailVerificationCodeSendResult send(String email, EmailVerificationPurpose verificationPurpose);
}
