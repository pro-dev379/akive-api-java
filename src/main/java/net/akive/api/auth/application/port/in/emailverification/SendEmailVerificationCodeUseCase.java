package net.akive.api.auth.application.port.in.emailverification;

import net.akive.api.auth.application.dto.result.emailverification.SendEmailVerificationResult;
import net.akive.api.auth.application.dto.command.emailverification.SendEmailVerificationCodeCommand;

public interface SendEmailVerificationCodeUseCase {

    SendEmailVerificationResult send(SendEmailVerificationCodeCommand command);
}
