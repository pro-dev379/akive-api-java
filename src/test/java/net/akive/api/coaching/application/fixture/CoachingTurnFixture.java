package net.akive.api.coaching.application.fixture;

import net.akive.api.coaching.application.event.CoachingTurnCompletedEvent;
import net.akive.api.coaching.application.provided.dto.CoachingTurnResult;

import java.time.Instant;
import java.util.UUID;

import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.COACHING_RESPONSE_CONTENT;
import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.EVENT_ID;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.COACHING_CONVERSATION_ID;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public final class CoachingTurnFixture {

    public static final UUID ANOTHER_EVENT_ID =
            UUID.fromString("08609705-5187-4956-872d-e78834230681");
    public static final Instant COACHING_TURN_COMPLETED_AT =
            Instant.parse("2026-10-17T00:00:00Z");

    private CoachingTurnFixture() {
    }

    public static CoachingTurnCompletedEvent createCoachingTurnCompletedEvent() {
        return createCoachingTurnCompletedEvent(EVENT_ID);
    }

    public static CoachingTurnCompletedEvent createCoachingTurnCompletedEvent(UUID eventId) {
        return new CoachingTurnCompletedEvent(
                eventId,
                USER_ID,
                COACHING_CONVERSATION_ID,
                COACHING_MESSAGE_ID,
                ANOTHER_COACHING_MESSAGE_ID,
                COACHING_TURN_COMPLETED_AT
        );
    }

    public static CoachingTurnResult createCoachingTurnResult() {
        return new CoachingTurnResult(MESSAGE_CONTENT, COACHING_RESPONSE_CONTENT);
    }
}
