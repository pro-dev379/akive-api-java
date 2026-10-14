package net.akive.api.learner.application.fixture;

import net.akive.api.learner.domain.InsightStatus;

import java.util.List;

import static net.akive.api.learner.domain.InsightStatus.ACTIVE;
import static net.akive.api.learner.domain.InsightStatus.CANDIDATE;

public final class LearnerInsightQueryFixture {

    public static final String QUERY = "작은 계획을 꾸준히 실천하는 학습 특성";
    public static final List<InsightStatus> FILTER_STATUSES = List.of(CANDIDATE, ACTIVE);
    public static final int SEARCH_RESULT_MAX_COUNT = 2;

    private LearnerInsightQueryFixture() {
    }
}
