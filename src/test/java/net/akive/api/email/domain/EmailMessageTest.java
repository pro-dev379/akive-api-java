package net.akive.api.email.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationError;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.EMPTY_COLLECTION;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.REQUIRED_VALUE_MISSING;
import static net.akive.api.email.domain.EmailMessageFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class EmailMessageTest {

    @Test
    void 이메일_메시지를_생성한다() {
        EmailMessage emailMessage = createEmailMessage();

        assertThat(emailMessage.from()).isEqualTo(FROM);
        assertThat(emailMessage.to()).isEqualTo(TO);
        assertThat(emailMessage.subject()).isEqualTo(SUBJECT);
        assertThat(emailMessage.content()).isEqualTo(CONTENT);
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void 송신자_혹은_수신자_정보가_비어있으면_이메일_메시지_생성에_실패한다(
            String from,
            List<String> to,
            String subject,
            String content,
            BusinessRuleViolationError expectedError
    ) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> new EmailMessage(from, to, subject, content))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(expectedError);
    }

    private static Stream<Arguments> invalidFields() {
        return Stream.of(
                arguments(null, TO, SUBJECT, CONTENT, REQUIRED_VALUE_MISSING),
                arguments(FROM, List.of(), SUBJECT, CONTENT, EMPTY_COLLECTION),
                arguments(FROM, null, SUBJECT, CONTENT, REQUIRED_VALUE_MISSING),
                arguments(FROM, TO, null, CONTENT, REQUIRED_VALUE_MISSING),
                arguments(FROM, TO, SUBJECT, null, REQUIRED_VALUE_MISSING)
        );
    }
}
