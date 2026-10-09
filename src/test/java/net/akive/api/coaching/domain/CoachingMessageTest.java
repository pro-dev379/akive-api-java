package net.akive.api.coaching.domain;

import org.junit.jupiter.api.Test;

import static net.akive.api.coaching.domain.MessageRole.ASSISTANT;
import static net.akive.api.coaching.domain.MessageRole.USER;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.createCoachingConversation;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(coachingMessage.getCreatedAt()).isEqualTo(MESSAGE_CREATED_AT);
    }

    @Test
    void 코칭_대화_메시지를_생성한다() {
        CoachingConversation coachingConversation = createCoachingConversation();

        CoachingMessage coachingMessage = CoachingMessage.create(
                coachingConversation,
                USER,
                "  " + MESSAGE_CONTENT + "  "
        );

        assertThat(coachingMessage.getCoachingMessageId()).isNull();
        assertThat(coachingMessage.getCoachingConversation()).isEqualTo(coachingConversation);
        assertThat(coachingMessage.getRole()).isEqualTo(USER);
        assertThat(coachingMessage.getContent()).isEqualTo(MESSAGE_CONTENT);
        assertThat(coachingMessage.getCreatedAt()).isNull();
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
        CoachingMessage coachingMessage = CoachingMessage.create(coachingConversation, USER, MESSAGE_CONTENT);
        CoachingMessage anotherCoachingMessage = CoachingMessage.create(coachingConversation, USER, MESSAGE_CONTENT);

        assertThat(coachingMessage).isNotEqualTo(anotherCoachingMessage);
    }
}
