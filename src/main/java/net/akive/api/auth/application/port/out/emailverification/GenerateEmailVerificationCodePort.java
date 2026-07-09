package net.akive.api.auth.application.port.out.emailverification;

import net.akive.api.auth.domain.emailverification.EmailVerificationCode;

public interface GenerateEmailVerificationCodePort {

    EmailVerificationCode generate();
}
