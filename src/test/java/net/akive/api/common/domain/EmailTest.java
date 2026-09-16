package net.akive.api.common.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationError;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
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
    void 이메일_값이_올바르지_않으면_생성이_거부된다(
            String email,
            BusinessRuleViolationError expectedError
    ) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> new Email(email))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(expectedError);
    }

    private static Stream<Arguments> invalidEmailValues() {
        return Stream.of(
                arguments("invalid-email", INVALID_FORMAT),
                arguments("   ", BLANK_VALUE),
                arguments(null, REQUIRED_VALUE_MISSING)
        );
    }
}
