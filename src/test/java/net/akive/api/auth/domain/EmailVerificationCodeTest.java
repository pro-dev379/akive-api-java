package net.akive.api.auth.domain;

import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import net.akive.api.auth.domain.emailverification.exception.EmailVerificationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.auth.domain.emailverification.exception.EmailVerificationErrorCode.INVALID_CODE_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EmailVerificationCodeTest {

    @Test
    void 인증번호_값_객체를_생성한다() {
        // when
        EmailVerificationCode verificationCode = EmailVerificationCode.of("123456");

        // then
        assertThat(verificationCode.value()).isEqualTo("123456");
    }

    @Test
    void 인증번호가_6자리_숫자가_아니면_생성이_거부된다() {
        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() -> EmailVerificationCode.of("12345a"))
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(INVALID_CODE_FORMAT);
    }
}
