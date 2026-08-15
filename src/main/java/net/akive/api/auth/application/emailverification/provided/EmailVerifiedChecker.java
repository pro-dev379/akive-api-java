package net.akive.api.auth.application.emailverification.provided;

import net.akive.api.auth.application.emailverification.provided.dto.EmailVerifiedCheckCommand;

public interface EmailVerifiedChecker {

    void checkVerified(EmailVerifiedCheckCommand command);
}
