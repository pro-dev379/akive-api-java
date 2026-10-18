package net.akive.api.coaching.domain.fixture;

import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.ConversationContext;
import net.akive.api.user.domain.User;

import java.time.Instant;

import static net.akive.api.coaching.domain.ConversationContext.MAIN;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;

public class CoachingConversationFixture {

    public static final Long COACHING_CONVERSATION_ID = 1L;
    public static final Long ANOTHER_COACHING_CONVERSATION_ID = 2L;
    public static final String LLM_CONVERSATION_ID = "conv_123";
    public static final String ANOTHER_LLM_CONVERSATION_ID = "conv_456";
    public static final String CONVERSATION_TITLE = "학습 계획 코칭";
    public static final Instant CREATED_AT = Instant.parse("2026-10-18T12:00:01Z");
    public static final Instant UPDATED_AT = Instant.parse("2026-10-18T12:00:01Z");

    private CoachingConversationFixture() {
    }

    public static CoachingConversation createCoachingConversation() {
        return createCoachingConversation(createUser(), LLM_CONVERSATION_ID, MAIN);
    }

    public static CoachingConversation createCoachingConversation(
            User user,
            String llmConversationId,
            ConversationContext context
    ) {
        return CoachingConversation.create(user, llmConversationId, context);
    }

    public static CoachingConversation reconstructCoachingConversation() {
        return reconstructCoachingConversation(
                COACHING_CONVERSATION_ID,
                createUser(),
                LLM_CONVERSATION_ID,
                CONVERSATION_TITLE,
                MAIN
        );
    }

    public static CoachingConversation reconstructCoachingConversation(
            Long coachingConversationId,
            User user,
            String llmConversationId,
            String title,
            ConversationContext context
    ) {
        return CoachingConversation.reconstruct(
                coachingConversationId,
                user,
                llmConversationId,
                title,
                context,
                CREATED_AT,
                UPDATED_AT
        );
    }
}
