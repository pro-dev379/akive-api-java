package net.akive.api.auth.infrastructure.adapter.emailverification;

import net.akive.api.auth.application.port.out.emailverification.GenerateEmailVerificationCodePort;
import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
class SecureRandomEmailVerificationCodeGenerator implements GenerateEmailVerificationCodePort {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public EmailVerificationCode generate() {
        int verificationCode = secureRandom.nextInt(1_000_000);
        return new EmailVerificationCode(String.format("%06d", verificationCode));
    }
}
