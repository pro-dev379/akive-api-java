package net.akive.api.common.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EmailTest {

    @Test
    void 이메일_값_객체를_생성한다() {
        // when
        Email email = Email.of("test@test.com");

        // then
        assertThat(email.value()).isEqualTo("test@test.com");
    }

    @Test
    void 이메일_형식이_올바르지_않으면_생성이_거부된다() {
        // when & then
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> Email.of("invalid-email"))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(INVALID_EMAIL_FORMAT);
    }

    @Test
    void 이메일이_비어있으면_생성이_거부된다() {
        // when & then
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> Email.of("   "))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 이메일이_null이면_생성이_거부된다() {
        // when & then
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> Email.of(null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 이메일이_최대_길이를_초과하면_생성이_거부된다() {
        // given
        String email = "a".repeat(192) + "@test.com";

        // when & then
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> Email.of(email))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }
}
