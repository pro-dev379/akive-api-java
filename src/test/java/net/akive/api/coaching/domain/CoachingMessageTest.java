package net.akive.api.coaching.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.coaching.domain.CoachingPolicy.USER_MESSAGE_CONTENT_MAX_LENGTH;
import static net.akive.api.coaching.domain.MessageCompletionStatus.COMPLETED;
import static net.akive.api.coaching.domain.MessageCompletionStatus.TRUNCATED;
import static net.akive.api.coaching.domain.MessageRole.ASSISTANT;
import static net.akive.api.coaching.domain.MessageRole.USER;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.createCoachingConversation;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CoachingMessageTest {

    @Test
    void 저장된_코칭_대화_메시지를_복원한다() {
        CoachingConversation coachingConversation = createCoachingConversation();

        CoachingMessage coachingMessage = reconstructCoachingMessage(
                COACHING_MESSAGE_ID,
                coachingConversation,
                USER,
                MESSAGE_CONTENT,
                MESSAGE_CREATED_AT
        );

        assertThat(coachingMessage.getCoachingMessageId()).isEqualTo(COACHING_MESSAGE_ID);
        assertThat(coachingMessage.getCoachingConversation()).isEqualTo(coachingConversation);
        assertThat(coachingMessage.getRole()).isEqualTo(USER);
        assertThat(coachingMessage.getContent()).isEqualTo(MESSAGE_CONTENT);
        assertThat(coachingMessage.getCompletionStatus()).isEqualTo(COMPLETED);
        assertThat(coachingMessage.getCreatedAt()).isEqualTo(MESSAGE_CREATED_AT);
    }

    @Test
    void 저장된_코칭_대화_메시지의_내용이_공백이면_복원할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> reconstructCoachingMessage(
                        COACHING_MESSAGE_ID,
                        createCoachingConversation(),
                        USER,
                        "  ",
                        COMPLETED,
                        MESSAGE_CREATED_AT
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 저장된_코칭_대화_메시지의_완료_상태가_없으면_복원할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> reconstructCoachingMessage(
                        COACHING_MESSAGE_ID,
                        createCoachingConversation(),
                        ASSISTANT,
                        MESSAGE_CONTENT,
                        null,
                        MESSAGE_CREATED_AT
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 저장된_회원_메시지가_내용_잘림_상태면_복원할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> reconstructCoachingMessage(
                        COACHING_MESSAGE_ID,
                        createCoachingConversation(),
                        USER,
                        MESSAGE_CONTENT,
                        TRUNCATED,
                        MESSAGE_CREATED_AT
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_COACHING_MESSAGE_COMPLETION_STATUS);
    }

    @Test
    void 코칭_대화_메시지를_생성한다() {
        CoachingConversation coachingConversation = createCoachingConversation();

        CoachingMessage coachingMessage = CoachingMessage.create(
                coachingConversation,
                USER,
                "  " + MESSAGE_CONTENT + "  ",
                COMPLETED
        );

        assertThat(coachingMessage.getCoachingMessageId()).isNull();
        assertThat(coachingMessage.getCoachingConversation()).isEqualTo(coachingConversation);
        assertThat(coachingMessage.getRole()).isEqualTo(USER);
        assertThat(coachingMessage.getContent()).isEqualTo(MESSAGE_CONTENT);
        assertThat(coachingMessage.getCompletionStatus()).isEqualTo(COMPLETED);
        assertThat(coachingMessage.getCreatedAt()).isNull();
    }

    @Test
    void 코칭_대화_메시지_생성_시_내용은_공백일_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> CoachingMessage.create(
                        createCoachingConversation(),
                        USER,
                        "  ",
                        COMPLETED
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 코칭_대화_메시지의_완료_상태가_없으면_생성할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> CoachingMessage.create(
                        createCoachingConversation(),
                        ASSISTANT,
                        MESSAGE_CONTENT,
                        null
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 회원_메시지를_생성한다() {
        CoachingConversation coachingConversation = createCoachingConversation();

        CoachingMessage coachingMessage = CoachingMessage.fromUser(
                coachingConversation,
                "  " + MESSAGE_CONTENT + "  "
        );

        assertThat(coachingMessage.getCoachingMessageId()).isNull();
        assertThat(coachingMessage.getCoachingConversation()).isEqualTo(coachingConversation);
        assertThat(coachingMessage.getRole()).isEqualTo(USER);
        assertThat(coachingMessage.getContent()).isEqualTo(MESSAGE_CONTENT);
        assertThat(coachingMessage.getCompletionStatus()).isEqualTo(COMPLETED);
        assertThat(coachingMessage.getCreatedAt()).isNull();
    }

    @Test
    void 회원_메시지_생성_시_내용은_최대_길이를_초과할_수_없다() {
        String content = "a".repeat(USER_MESSAGE_CONTENT_MAX_LENGTH + 1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> CoachingMessage.create(
                        createCoachingConversation(),
                        USER,
                        content,
                        COMPLETED
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 회원_메시지_생성_시_내용_잘림_상태일_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> CoachingMessage.create(
                        createCoachingConversation(),
                        USER,
                        MESSAGE_CONTENT,
                        TRUNCATED
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_COACHING_MESSAGE_COMPLETION_STATUS);
    }

    @Test
    void AI_코치_메시지를_생성한다() {
        CoachingConversation coachingConversation = createCoachingConversation();

        CoachingMessage coachingMessage = CoachingMessage.fromAssistant(
                coachingConversation,
                "  " + MESSAGE_CONTENT + "  ",
                COMPLETED
        );

        assertThat(coachingMessage.getCoachingMessageId()).isNull();
        assertThat(coachingMessage.getCoachingConversation()).isEqualTo(coachingConversation);
        assertThat(coachingMessage.getRole()).isEqualTo(ASSISTANT);
        assertThat(coachingMessage.getContent()).isEqualTo(MESSAGE_CONTENT);
        assertThat(coachingMessage.getCompletionStatus()).isEqualTo(COMPLETED);
        assertThat(coachingMessage.getCreatedAt()).isNull();
    }

    @Test
    void AI_코치_메시지_생성_시_내용_잘림_상태일_수_있다() {
        CoachingMessage coachingMessage = CoachingMessage.fromAssistant(
                createCoachingConversation(),
                MESSAGE_CONTENT,
                TRUNCATED
        );

        assertThat(coachingMessage.getCompletionStatus()).isEqualTo(TRUNCATED);
    }

    @Test
    void 같은_식별자를_가진_코칭_대화_메시지는_같게_취급한다() {
        CoachingMessage coachingMessage = reconstructCoachingMessage();
        CoachingMessage sameCoachingMessage = reconstructCoachingMessage(
                COACHING_MESSAGE_ID,
                createCoachingConversation(),
                ASSISTANT,
                "다른 내용",
                MESSAGE_CREATED_AT.plusSeconds(1)
        );

        assertThat(coachingMessage).isEqualTo(sameCoachingMessage);
        assertThat(coachingMessage.hashCode()).isEqualTo(sameCoachingMessage.hashCode());
    }

    @Test
    void 식별자가_다른_코칭_대화_메시지는_다르게_취급한다() {
        CoachingMessage coachingMessage = reconstructCoachingMessage();
        CoachingMessage anotherCoachingMessage = reconstructCoachingMessage(
                ANOTHER_COACHING_MESSAGE_ID,
                createCoachingConversation(),
                USER,
                MESSAGE_CONTENT,
                MESSAGE_CREATED_AT
        );

        assertThat(coachingMessage).isNotEqualTo(anotherCoachingMessage);
    }

    @Test
    void 식별자가_없는_코칭_대화_메시지끼리는_나머지_값이_같더라도_다르게_취급한다() {
        CoachingConversation coachingConversation = createCoachingConversation();
        CoachingMessage coachingMessage = CoachingMessage.create(
                coachingConversation,
                USER,
                MESSAGE_CONTENT,
                COMPLETED
        );
        CoachingMessage anotherCoachingMessage = CoachingMessage.create(
                coachingConversation,
                USER,
                MESSAGE_CONTENT,
                COMPLETED
        );

        assertThat(coachingMessage).isNotEqualTo(anotherCoachingMessage);
    }
}
