package net.akive.api.auth.adapter.emailverification.generation;

import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import org.junit.jupiter.api.Test;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_LENGTH;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_PATTERN;
import static org.assertj.core.api.Assertions.assertThat;


class SecureRandomEmailVerificationCodeGeneratorTest {

    SecureRandomEmailVerificationCodeGenerator generator = new SecureRandomEmailVerificationCodeGenerator();

    @Test
    void 숫자_6자리_이메일_인증번호를_생성한다() {
        EmailVerificationCode verificationCode = generator.generate();

        assertThat(verificationCode.value()).hasSize(VERIFICATION_CODE_LENGTH).matches(VERIFICATION_CODE_PATTERN);
    }
}
