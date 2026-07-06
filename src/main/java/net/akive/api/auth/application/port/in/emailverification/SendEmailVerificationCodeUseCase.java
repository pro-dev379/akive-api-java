package net.akive.api.auth.application.port.in.emailverification;

import net.akive.api.auth.application.dto.result.emailverification.SendEmailVerificationCodeResult;
import net.akive.api.auth.application.dto.command.emailverification.SendEmailVerificationCodeCommand;

public interface SendEmailVerificationCodeUseCase {

    SendEmailVerificationCodeResult send(SendEmailVerificationCodeCommand command);
}
