package net.akive.api.auth.application.port.in.emailverification;

import net.akive.api.auth.application.dto.result.emailverification.SendEmailVerificationResult;
import net.akive.api.auth.application.dto.command.emailverification.SendEmailVerificationCommand;

public interface SendEmailVerificationCodeUseCase {

    SendEmailVerificationResult send(SendEmailVerificationCommand command);
}
