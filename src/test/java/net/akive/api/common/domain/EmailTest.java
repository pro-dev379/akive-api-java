package net.akive.api.common.domain;

import net.akive.api.common.exception.business.BadRequestError;
import net.akive.api.common.exception.business.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BadRequestError.BLANK_VALUE;
import static net.akive.api.common.exception.business.BadRequestError.INVALID_EMAIL_FORMAT;
import static net.akive.api.common.exception.business.BadRequestError.REQUIRED_VALUE_MISSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class EmailTest {

    @Test
    void 이메일_값_객체를_생성한다() {
        Email email = new Email("test@test.com");

        assertThat(email.value()).isEqualTo("test@test.com");
    }

    @ParameterizedTest
    @MethodSource("invalidEmailValues")
    void 이메일_값이_유효하지_않으면_생성이_거부된다(
            String email,
            BadRequestError expectedError
    ) {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new Email(email))
                .extracting(BadRequestException::getError)
                .isEqualTo(expectedError);
    }

    private static Stream<Arguments> invalidEmailValues() {
        return Stream.of(
                arguments("invalid-email", INVALID_EMAIL_FORMAT),
                arguments("   ", BLANK_VALUE),
                arguments(null, REQUIRED_VALUE_MISSING)
        );
    }
}
