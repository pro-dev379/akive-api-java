package net.akive.api.auth.application.port.in.emailverification;

import net.akive.api.auth.application.dto.EmailVerificationCodeSendResult;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

public interface SendEmailVerificationCodeUseCase {

    EmailVerificationCodeSendResult send(String email, EmailVerificationPurpose verificationPurpose);
}
