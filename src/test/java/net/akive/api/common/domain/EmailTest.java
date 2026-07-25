package net.akive.api.common.domain;

import net.akive.api.common.exception.business.BadRequestException;

import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BadRequestError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EmailTest {

    @Test
    void 이메일_값_객체를_생성한다() {
        // when
        Email email = new Email("test@test.com");

        // then
        assertThat(email.value()).isEqualTo("test@test.com");
    }

    @Test
    void 이메일_형식이_올바르지_않으면_생성이_거부된다() {
        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new Email("invalid-email"))
                .extracting(BadRequestException::getError)
                .isEqualTo(INVALID_EMAIL_FORMAT);
    }

    @Test
    void 이메일이_비어있으면_생성이_거부된다() {
        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new Email("   "))
                .extracting(BadRequestException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 이메일이_null이면_생성이_거부된다() {
        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new Email(null))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }
}
