package net.akive.api.learner.application.fixture;

import net.akive.api.learner.application.event.LearnerInsightChangedEvent;

import java.time.Instant;
import java.util.UUID;

import static net.akive.api.learner.domain.InsightConfidence.LOW;
import static net.akive.api.learner.domain.InsightStatus.CANDIDATE;
import static net.akive.api.learner.domain.InsightType.BEHAVIOR_PATTERN;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.INSIGHT_CONTENT;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.LEARNER_INSIGHT_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public final class LearnerInsightEventFixture {

    public static final UUID LEARNER_INSIGHT_CHANGED_EVENT_ID =
            UUID.fromString("d032ed06-de8f-4f49-9489-535973ed3126");
    public static final UUID ANOTHER_LEARNER_INSIGHT_CHANGED_EVENT_ID =
            UUID.fromString("d3d2e508-2f66-4136-ac9e-7a7d266573cb");
    public static final Instant LEARNER_INSIGHT_CHANGED_AT =
            Instant.parse("2026-10-17T01:00:00Z");

    private LearnerInsightEventFixture() {
    }

    public static LearnerInsightChangedEvent createLearnerInsightChangedEvent() {
        return createLearnerInsightChangedEvent(LEARNER_INSIGHT_CHANGED_EVENT_ID);
    }

    public static LearnerInsightChangedEvent createLearnerInsightChangedEvent(UUID eventId) {
        return new LearnerInsightChangedEvent(
                eventId,
                USER_ID,
                LEARNER_INSIGHT_ID,
                BEHAVIOR_PATTERN,
                INSIGHT_CONTENT,
                LOW,
                CANDIDATE,
                LEARNER_INSIGHT_CHANGED_AT
        );
    }
}
