package net.akive.api.auth.email.infrastructure;

import net.akive.api.auth.email.application.port.out.EmailAuthCodeGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureRandomEmailAuthCodeGenerator implements EmailAuthCodeGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        int authCode = secureRandom.nextInt(1_000_000);
        return String.format("%06d", authCode);
    }
}
