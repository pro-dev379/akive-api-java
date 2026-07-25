package net.akive.api.auth.infrastructure.adapter.emailverification;

import net.akive.api.auth.application.port.out.emailverification.GenerateEmailVerificationCodePort;
import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.*;

@Component
class SecureRandomEmailVerificationCodeGenerator implements GenerateEmailVerificationCodePort {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public EmailVerificationCode generate() {
        int verificationCode = secureRandom.nextInt(VERIFICATION_CODE_BOUND);
        return new EmailVerificationCode(String.format("%0" + VERIFICATION_CODE_LENGTH + "d", verificationCode));
    }
}
