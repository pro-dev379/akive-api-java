package net.akive.api.coaching.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import static net.akive.api.coaching.domain.ConversationContext.MAIN;
import static net.akive.api.coaching.domain.ConversationContext.REPORT;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.BLANK_VALUE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.REQUIRED_VALUE_MISSING;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CoachingConversationTest {

    @Test
    void 저장된_코칭_대화를_복원한다() {
        User user = createUser();

        CoachingConversation coachingConversation = reconstructCoachingConversation(
                COACHING_CONVERSATION_ID,
                user,
                LLM_CONVERSATION_ID,
                "  " + CONVERSATION_TITLE + "  ",
                MAIN
        );

        assertThat(coachingConversation.getCoachingConversationId()).isEqualTo(COACHING_CONVERSATION_ID);
        assertThat(coachingConversation.getUser()).isEqualTo(user);
        assertThat(coachingConversation.getLlmConversationId()).isEqualTo(LLM_CONVERSATION_ID);
        assertThat(coachingConversation.getTitle()).isEqualTo(CONVERSATION_TITLE);
        assertThat(coachingConversation.getContext()).isEqualTo(MAIN);
    }

    @Test
    void 제목이_없는_코칭_대화를_복원한다() {
        CoachingConversation coachingConversation = reconstructCoachingConversation(
                COACHING_CONVERSATION_ID,
                createUser(),
                LLM_CONVERSATION_ID,
                null,
                MAIN
        );

        assertThat(coachingConversation.getTitle()).isNull();
    }

    @Test
    void 저장된_코칭_대화에_LLM_대화_식별자가_없으면_복원할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> reconstructCoachingConversation(
                        COACHING_CONVERSATION_ID,
                        createUser(),
                        null,
                        CONVERSATION_TITLE,
                        MAIN
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 저장된_코칭_대화의_제목이_공백이면_복원할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> reconstructCoachingConversation(
                        COACHING_CONVERSATION_ID,
                        createUser(),
                        LLM_CONVERSATION_ID,
                        "  ",
                        MAIN
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 코칭_대화를_생성한다() {
        User user = createUser();

        CoachingConversation coachingConversation = CoachingConversation.create(
                user,
                "  " + LLM_CONVERSATION_ID + "  ",
                MAIN
        );

        assertThat(coachingConversation.getCoachingConversationId()).isNull();
        assertThat(coachingConversation.getUser()).isEqualTo(user);
        assertThat(coachingConversation.getLlmConversationId()).isEqualTo(LLM_CONVERSATION_ID);
        assertThat(coachingConversation.getTitle()).isNull();
        assertThat(coachingConversation.getContext()).isEqualTo(MAIN);
    }

    @Test
    void 코칭_대화_생성_시_LLM_대화_식별자는_공백일_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> CoachingConversation.create(
                        createUser(),
                        "  ",
                        MAIN
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 코칭_대화의_제목을_수정한다() {
        CoachingConversation coachingConversation = reconstructCoachingConversation(
                COACHING_CONVERSATION_ID,
                createUser(),
                LLM_CONVERSATION_ID,
                null,
                MAIN
        );

        coachingConversation.updateTitle("  " + CONVERSATION_TITLE + "  ");

        assertThat(coachingConversation.getTitle()).isEqualTo(CONVERSATION_TITLE);
    }

    @Test
    void 코칭_대화의_제목은_공백으로_수정할_수_없다() {
        CoachingConversation coachingConversation = reconstructCoachingConversation();

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> coachingConversation.updateTitle("  "))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 같은_식별자를_가진_코칭_대화는_같게_취급한다() {
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        CoachingConversation sameCoachingConversation = reconstructCoachingConversation(
                COACHING_CONVERSATION_ID,
                createUser(),
                "conv_456",
                "다른 제목",
                REPORT
        );

        assertThat(coachingConversation).isEqualTo(sameCoachingConversation);
        assertThat(coachingConversation.hashCode()).isEqualTo(sameCoachingConversation.hashCode());
    }

    @Test
    void 식별자가_다른_코칭_대화는_다르게_취급한다() {
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        CoachingConversation anotherCoachingConversation = reconstructCoachingConversation(
                ANOTHER_COACHING_CONVERSATION_ID,
                createUser(),
                LLM_CONVERSATION_ID,
                CONVERSATION_TITLE,
                MAIN
        );

        assertThat(coachingConversation).isNotEqualTo(anotherCoachingConversation);
    }

    @Test
    void 식별자가_없는_코칭_대화끼리는_나머지_값이_같더라도_다르게_취급한다() {
        User user = createUser();
        CoachingConversation coachingConversation = CoachingConversation.create(user, LLM_CONVERSATION_ID, MAIN);
        CoachingConversation anotherCoachingConversation = CoachingConversation.create(user, LLM_CONVERSATION_ID, MAIN);

        assertThat(coachingConversation).isNotEqualTo(anotherCoachingConversation);
    }
}
