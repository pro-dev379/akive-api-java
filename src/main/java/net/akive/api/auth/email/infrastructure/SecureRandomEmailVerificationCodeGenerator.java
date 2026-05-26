package net.akive.api.auth.email.infrastructure;

import net.akive.api.auth.email.application.port.out.EmailVerificationCodeGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureRandomEmailVerificationCodeGenerator implements EmailVerificationCodeGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        int verificationCode = secureRandom.nextInt(1_000_000);
        return String.format("%06d", verificationCode);
    }
}
