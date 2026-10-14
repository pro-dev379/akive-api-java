package net.akive.api.coaching.domain.fixture;

import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.CoachingMessage;
import net.akive.api.coaching.domain.MessageCompletionStatus;
import net.akive.api.coaching.domain.MessageRole;

import java.time.Instant;

import static net.akive.api.coaching.domain.MessageCompletionStatus.COMPLETED;
import static net.akive.api.coaching.domain.MessageRole.ASSISTANT;
import static net.akive.api.coaching.domain.MessageRole.USER;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.createCoachingConversation;

public class CoachingMessageFixture {

    public static final Long COACHING_MESSAGE_ID = 1L;
    public static final Long ANOTHER_COACHING_MESSAGE_ID = 2L;
    public static final String MESSAGE_CONTENT = "코칭을 시작하고 싶어요.";
    public static final Instant MESSAGE_CREATED_AT = Instant.parse("2026-10-09T00:00:00Z");

    private CoachingMessageFixture() {
    }

    public static CoachingMessage reconstructCoachingMessage() {
        return reconstructCoachingMessage(
                COACHING_MESSAGE_ID,
                createCoachingConversation(),
                USER,
                MESSAGE_CONTENT,
                MESSAGE_CREATED_AT
        );
    }

    public static CoachingMessage reconstructUserMessage(CoachingConversation coachingConversation) {
        return reconstructCoachingMessage(
                COACHING_MESSAGE_ID,
                coachingConversation,
                USER,
                MESSAGE_CONTENT,
                MESSAGE_CREATED_AT
        );
    }

    public static CoachingMessage reconstructAssistantMessage(
            CoachingConversation coachingConversation,
            String content
    ) {
        return reconstructAssistantMessage(coachingConversation, content, COMPLETED);
    }

    public static CoachingMessage reconstructAssistantMessage(
            CoachingConversation coachingConversation,
            String content,
            MessageCompletionStatus completionStatus
    ) {
        return reconstructCoachingMessage(
                ANOTHER_COACHING_MESSAGE_ID,
                coachingConversation,
                ASSISTANT,
                content,
                completionStatus,
                MESSAGE_CREATED_AT
        );
    }

    public static CoachingMessage reconstructCoachingMessage(
            Long coachingMessageId,
            CoachingConversation coachingConversation,
            MessageRole role,
            String content,
            Instant createdAt
    ) {
        return reconstructCoachingMessage(
                coachingMessageId,
                coachingConversation,
                role,
                content,
                COMPLETED,
                createdAt
        );
    }

    public static CoachingMessage reconstructCoachingMessage(
            Long coachingMessageId,
            CoachingConversation coachingConversation,
            MessageRole role,
            String content,
            MessageCompletionStatus completionStatus,
            Instant createdAt
    ) {
        return CoachingMessage.reconstruct(
                coachingMessageId,
                coachingConversation,
                role,
                content,
                completionStatus,
                createdAt
        );
    }
}
