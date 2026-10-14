package net.akive.api.learner.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.learner.domain.LearnerPolicy.EVIDENCE_CONTENT_MAX_LENGTH;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class LearnerInsightEvidence {

    private Long learnerInsightEvidenceId;

    private LearnerInsight learnerInsight;

    private EvidenceSourceType sourceType;

    private Long sourceId;

    private EvidenceStance stance;

    private String content;

    private Instant createdAt;

    public static LearnerInsightEvidence reconstruct(
            Long learnerInsightEvidenceId,
            LearnerInsight learnerInsight,
            EvidenceSourceType sourceType,
            Long sourceId,
            EvidenceStance stance,
            String content,
            Instant createdAt
    ) {
        return LearnerInsightEvidence.builder()
                .learnerInsightEvidenceId(requirePositive(learnerInsightEvidenceId))
                .learnerInsight(requireNonNull(learnerInsight))
                .sourceType(requireNonNull(sourceType))
                .sourceId(requirePositive(sourceId))
                .stance(requireNonNull(stance))
                .content(validateContent(content))
                .createdAt(requireNonNull(createdAt))
                .build();
    }

    public static LearnerInsightEvidence create(
            LearnerInsight learnerInsight,
            EvidenceSourceType sourceType,
            Long sourceId,
            EvidenceStance stance,
            String content,
            Instant observedAt
    ) {
        LearnerInsight validatedLearnerInsight = requireNonNull(learnerInsight);
        EvidenceSourceType validatedSourceType = requireNonNull(sourceType);
        Long validatedSourceId = requirePositive(sourceId);
        EvidenceStance validatedStance = requireNonNull(stance);
        String validatedContent = validateContent(content);

        validatedLearnerInsight.recordEvidence(observedAt);

        return LearnerInsightEvidence.builder()
                .learnerInsight(validatedLearnerInsight)
                .sourceType(validatedSourceType)
                .sourceId(validatedSourceId)
                .stance(validatedStance)
                .content(validatedContent)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LearnerInsightEvidence that = (LearnerInsightEvidence) o;

        return this.learnerInsightEvidenceId != null
                && Objects.equals(this.learnerInsightEvidenceId, that.learnerInsightEvidenceId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private static String validateContent(String content) {
        return requireMaxLength(requireNotBlank(content), EVIDENCE_CONTENT_MAX_LENGTH);
    }
}
