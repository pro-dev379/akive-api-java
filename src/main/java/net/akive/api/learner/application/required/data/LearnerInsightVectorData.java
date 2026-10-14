package net.akive.api.learner.application.required.data;

import net.akive.api.learner.domain.InsightStatus;

import static net.akive.api.common.domain.DomainValidator.*;

public record LearnerInsightVectorData(
        Long learnerInsightId,
        Long userId,
        InsightStatus status
) {

    public LearnerInsightVectorData {
        requirePositive(learnerInsightId);
        requirePositive(userId);
        requireNonNull(status);
    }
}
