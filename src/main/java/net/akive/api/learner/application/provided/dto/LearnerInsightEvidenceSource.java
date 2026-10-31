package net.akive.api.learner.application.provided.dto;

import net.akive.api.learner.domain.EvidenceSourceType;

import java.time.Instant;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record LearnerInsightEvidenceSource(
        EvidenceSourceType sourceType,
        Long sourceId,
        Instant observedAt
) {

    public LearnerInsightEvidenceSource {
        requireNonNull(sourceType);
        requirePositive(sourceId);
        requireNonNull(observedAt);
    }
}
