package net.akive.api.auth.application.port.in.emailverification;

import net.akive.api.auth.application.dto.result.EmailVerificationCodeSendResult;
import net.akive.api.auth.application.dto.command.emailverification.EmailVerificationCodeSendCommand;

public interface SendEmailVerificationCodeUseCase {

    EmailVerificationCodeSendResult send(EmailVerificationCodeSendCommand command);
}
