package net.akive.api.auth.infrastructure.emailverification;

import net.akive.api.auth.application.port.out.emailverification.EmailVerificationCodeGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
class SecureRandomEmailVerificationCodeGenerator implements EmailVerificationCodeGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        int verificationCode = secureRandom.nextInt(1_000_000);
        return String.format("%06d", verificationCode);
    }
}
