package net.akive.api.auth.application.emailverification.provided;

import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationCodeVerifyCommand;

public interface EmailVerificationCodeVerifier {

    void verify(EmailVerificationCodeVerifyCommand command);
}
