package net.akive.api.coaching.application.service.fixture;

import net.akive.api.coaching.application.provided.dto.CoachingTurnFindQuery;

import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.COACHING_CONVERSATION_ID;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.ANOTHER_COACHING_MESSAGE_ID;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.COACHING_MESSAGE_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class CoachingTurnQueryFixture {

    private CoachingTurnQueryFixture() {
    }

    public static CoachingTurnFindQuery createCoachingTurnFindQuery() {
        return new CoachingTurnFindQuery(
                USER_ID,
                COACHING_CONVERSATION_ID,
                COACHING_MESSAGE_ID,
                ANOTHER_COACHING_MESSAGE_ID
        );
    }
}
