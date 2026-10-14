package net.akive.api.learner.application.provided.dto;

import net.akive.api.learner.domain.EvidenceSourceType;

import java.time.Instant;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record LearnerInsightEvidenceSourceData(
        EvidenceSourceType sourceType,
        Long sourceId,
        Instant observedAt
) {

    public LearnerInsightEvidenceSourceData {
        requireNonNull(sourceType);
        requirePositive(sourceId);
        requireNonNull(observedAt);
    }
}
