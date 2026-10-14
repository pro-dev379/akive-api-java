package net.akive.api.coaching.application.fixture;

import net.akive.api.coaching.application.provided.dto.CoachingMessageSendCommand;
import net.akive.api.coaching.application.required.data.CoachingTurnRecoveryData;
import net.akive.api.coaching.domain.MessageCompletionStatus;

import java.util.UUID;

import static net.akive.api.coaching.domain.MessageCompletionStatus.COMPLETED;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.COACHING_CONVERSATION_ID;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.COACHING_MESSAGE_ID;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.MESSAGE_CONTENT;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class CoachingMessageCommandFixture {

    public static final UUID EVENT_ID = UUID.fromString("0199a8e2-7c31-7abc-8123-456789abcdef");
    public static final String COACHING_RESPONSE_CONTENT = "계획을 작은 단위로 나누어 보세요.";

    private CoachingMessageCommandFixture() {
    }

    public static CoachingMessageSendCommand createCoachingMessageSendCommand() {
        return new CoachingMessageSendCommand(USER_ID, COACHING_CONVERSATION_ID, MESSAGE_CONTENT);
    }

    public static CoachingTurnRecoveryData createCoachingTurnRecoveryData() {
        return createCoachingTurnRecoveryData(COACHING_RESPONSE_CONTENT, COMPLETED);
    }

    public static CoachingTurnRecoveryData createCoachingTurnRecoveryData(
            String content,
            MessageCompletionStatus completionStatus
    ) {
        return new CoachingTurnRecoveryData(EVENT_ID, COACHING_MESSAGE_ID, content, completionStatus);
    }
}
