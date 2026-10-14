package net.akive.api.learner.domain.fixture;

import net.akive.api.learner.domain.InsightConfidence;
import net.akive.api.learner.domain.InsightStatus;
import net.akive.api.learner.domain.InsightType;
import net.akive.api.learner.domain.LearnerInsight;
import net.akive.api.user.domain.User;

import java.time.Instant;

import static net.akive.api.learner.domain.InsightConfidence.LOW;
import static net.akive.api.learner.domain.InsightStatus.CANDIDATE;
import static net.akive.api.learner.domain.InsightType.BEHAVIOR_PATTERN;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;

public final class LearnerInsightFixture {

    public static final Long LEARNER_INSIGHT_ID = 1L;
    public static final Long ANOTHER_LEARNER_INSIGHT_ID = 2L;
    public static final Long THIRD_LEARNER_INSIGHT_ID = 3L;
    public static final String INSIGHT_CONTENT = "큰 계획보다 작은 단위의 계획을 꾸준히 실천한다.";
    public static final String ANOTHER_INSIGHT_CONTENT = "짧은 계획을 세우면 실행률이 높아진다.";
    public static final Instant FIRST_OBSERVED_AT = Instant.parse("2026-10-15T00:00:00Z");
    public static final Instant LAST_OBSERVED_AT = Instant.parse("2026-10-16T00:00:00Z");

    private LearnerInsightFixture() {
    }

    public static LearnerInsight createLearnerInsight(User user) {
        return createLearnerInsight(user, CANDIDATE);
    }

    public static LearnerInsight createLearnerInsight(User user, InsightStatus status) {
        return createLearnerInsight(user, INSIGHT_CONTENT, status);
    }

    public static LearnerInsight createLearnerInsight(
            User user,
            String content,
            InsightStatus status
    ) {
        LearnerInsight learnerInsight = LearnerInsight.create(
                user,
                BEHAVIOR_PATTERN,
                content,
                FIRST_OBSERVED_AT
        );

        switch (status) {
            case CANDIDATE -> {
            }
            case ACTIVE -> learnerInsight.reinforce();
            case SUPERSEDED -> learnerInsight.supersede();
        }

        return learnerInsight;
    }

    public static LearnerInsight reconstructLearnerInsight() {
        return reconstructLearnerInsight(LEARNER_INSIGHT_ID);
    }

    public static LearnerInsight reconstructLearnerInsight(Long learnerInsightId) {
        return reconstructLearnerInsight(
                learnerInsightId,
                createReconstructedUser(),
                BEHAVIOR_PATTERN,
                INSIGHT_CONTENT,
                LOW,
                CANDIDATE,
                FIRST_OBSERVED_AT,
                LAST_OBSERVED_AT
        );
    }

    public static LearnerInsight reconstructLearnerInsight(
            InsightConfidence confidence,
            InsightStatus status
    ) {
        return reconstructLearnerInsight(
                LEARNER_INSIGHT_ID,
                createReconstructedUser(),
                BEHAVIOR_PATTERN,
                INSIGHT_CONTENT,
                confidence,
                status,
                FIRST_OBSERVED_AT,
                LAST_OBSERVED_AT
        );
    }

    public static LearnerInsight reconstructLearnerInsight(
            Long learnerInsightId,
            User user,
            InsightType insightType,
            String content,
            InsightConfidence confidence,
            InsightStatus status,
            Instant firstObservedAt,
            Instant lastObservedAt
    ) {
        return LearnerInsight.reconstruct(
                learnerInsightId,
                user,
                insightType,
                content,
                confidence,
                status,
                firstObservedAt,
                lastObservedAt
        );
    }

    public static LearnerInsight reconstructSupersededLearnerInsight() {
        LearnerInsight learnerInsight = reconstructLearnerInsight();
        learnerInsight.supersede();

        return learnerInsight;
    }
}
