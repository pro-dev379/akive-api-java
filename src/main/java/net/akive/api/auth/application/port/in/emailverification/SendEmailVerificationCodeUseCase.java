package net.akive.api.auth.application.port.in.emailverification;

import net.akive.api.auth.application.dto.EmailVerificationCodeSendResult;
import net.akive.api.auth.application.dto.EmailVerificationCodeSendCommand;

public interface SendEmailVerificationCodeUseCase {

    EmailVerificationCodeSendResult send(EmailVerificationCodeSendCommand command);
}
