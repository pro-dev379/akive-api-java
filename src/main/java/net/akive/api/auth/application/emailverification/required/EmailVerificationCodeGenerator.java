package net.akive.api.auth.application.emailverification.required;

import net.akive.api.auth.domain.emailverification.EmailVerificationCode;

public interface EmailVerificationCodeGenerator {

    EmailVerificationCode generate();
}
