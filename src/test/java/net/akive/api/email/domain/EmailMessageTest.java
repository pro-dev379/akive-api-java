package net.akive.api.email.domain;

import net.akive.api.common.exception.business.BadRequestError;
import net.akive.api.common.exception.business.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BadRequestError.EMPTY_COLLECTION;
import static net.akive.api.common.exception.business.BadRequestError.REQUIRED_VALUE_MISSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class EmailMessageTest {

    @Test
    void 이메일_메시지를_생성한다() {
        EmailMessage emailMessage = new EmailMessage(
                "from@test.com",
                List.of("to@test.com"),
                "subject",
                "content"
        );

        assertThat(emailMessage.from()).isEqualTo("from@test.com");
        assertThat(emailMessage.to()).containsExactly("to@test.com");
        assertThat(emailMessage.subject()).isEqualTo("subject");
        assertThat(emailMessage.content()).isEqualTo("content");
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void 송신자_혹은_수신자_정보가_비어있으면_이메일_메시지_생성에_실패한다(
            String from,
            List<String> to,
            String subject,
            String content,
            BadRequestError expectedError
    ) {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new EmailMessage(from, to, subject, content))
                .extracting(BadRequestException::getError)
                .isEqualTo(expectedError);
    }

    private static Stream<Arguments> invalidFields() {
        return Stream.of(
                arguments(null, List.of("to@test.com"), "subject", "content", REQUIRED_VALUE_MISSING),
                arguments("from@test.com", List.of(), "subject", "content", EMPTY_COLLECTION),
                arguments("from@test.com", null, "subject", "content", REQUIRED_VALUE_MISSING),
                arguments("from@test.com", List.of("to@test.com"), null, "content", REQUIRED_VALUE_MISSING),
                arguments("from@test.com", List.of("to@test.com"), "subject", null, REQUIRED_VALUE_MISSING)
        );
    }
}
