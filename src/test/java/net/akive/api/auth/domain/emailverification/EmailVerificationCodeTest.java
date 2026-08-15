package net.akive.api.auth.domain.emailverification;

import net.akive.api.common.exception.business.BadRequestException;

import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BadRequestError.EMAIL_VERIFICATION_INVALID_CODE_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EmailVerificationCodeTest {

    @Test
    void 인증번호_값_객체를_생성한다() {
        String verificationCode = new EmailVerificationCode("123456").value();

        assertThat(verificationCode).isEqualTo("123456");
    }

    @Test
    void 인증번호가_6자리_숫자가_아니면_생성이_거부된다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new EmailVerificationCode("12345a"))
                .extracting(BadRequestException::getError)
                .isEqualTo(EMAIL_VERIFICATION_INVALID_CODE_FORMAT);
    }
}
