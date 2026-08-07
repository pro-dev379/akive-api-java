package net.akive.api.auth.application.emailverification.provided;

import net.akive.api.auth.application.emailverification.provided.dto.SendEmailVerificationResult;
import net.akive.api.auth.application.emailverification.provided.dto.SendEmailVerificationCommand;

public interface SendEmailVerificationCodeUseCase {

    SendEmailVerificationResult send(SendEmailVerificationCommand command);
}
