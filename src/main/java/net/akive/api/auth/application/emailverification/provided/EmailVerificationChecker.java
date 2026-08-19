package net.akive.api.auth.application.emailverification.provided;

import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationEnsureCommand;
import net.akive.api.common.exception.business.BadRequestException;

public interface EmailVerificationChecker {

    void ensureEmailVerified(EmailVerificationEnsureCommand command) throws BadRequestException;
}
