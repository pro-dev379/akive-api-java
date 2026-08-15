package net.akive.api.auth.adapter.emailverification.generation;

import net.akive.api.auth.application.emailverification.required.EmailVerificationCodeGenerator;
import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_BOUND;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_LENGTH;

@Component
class SecureRandomEmailVerificationCodeGenerator implements EmailVerificationCodeGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public EmailVerificationCode generate() {
        int verificationCode = secureRandom.nextInt(VERIFICATION_CODE_BOUND);
        return new EmailVerificationCode(String.format("%0" + VERIFICATION_CODE_LENGTH + "d", verificationCode));
    }
}
