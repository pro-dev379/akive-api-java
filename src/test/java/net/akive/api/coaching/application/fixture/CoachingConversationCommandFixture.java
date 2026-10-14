package net.akive.api.coaching.application.fixture;

import net.akive.api.coaching.application.provided.dto.CoachingConversationCreateCommand;

import static net.akive.api.coaching.domain.ConversationContext.MAIN;

public class CoachingConversationCommandFixture {

    private CoachingConversationCommandFixture() {
    }

    public static CoachingConversationCreateCommand createCoachingConversationCreateCommand(Long userId) {
        return new CoachingConversationCreateCommand(userId, MAIN);
    }
}
