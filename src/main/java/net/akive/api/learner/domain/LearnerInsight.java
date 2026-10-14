package net.akive.api.learner.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.user.domain.User;

import java.time.Instant;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_LEARNER_INSIGHT_OBSERVATION_PERIOD;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_LEARNER_INSIGHT_STATUS_AND_CONFIDENCE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_LEARNER_INSIGHT_STATUS_TRANSITION;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.LEARNER_INSIGHT_EVIDENCE_ADDITION_NOT_ALLOWED;
import static net.akive.api.learner.domain.InsightConfidence.HIGH;
import static net.akive.api.learner.domain.InsightConfidence.LOW;
import static net.akive.api.learner.domain.InsightConfidence.MEDIUM;
import static net.akive.api.learner.domain.InsightStatus.ACTIVE;
import static net.akive.api.learner.domain.InsightStatus.CANDIDATE;
import static net.akive.api.learner.domain.InsightStatus.SUPERSEDED;
import static net.akive.api.learner.domain.LearnerPolicy.INSIGHT_CONTENT_MAX_LENGTH;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class LearnerInsight {

    private Long learnerInsightId;

    private User user;

    private InsightType insightType;

    private String content;

    private InsightConfidence confidence;

    private InsightStatus status;

    private Instant firstObservedAt;

    private Instant lastObservedAt;

    public static LearnerInsight reconstruct(
            Long learnerInsightId,
            User user,
            InsightType insightType,
            String content,
            InsightConfidence confidence,
            InsightStatus status,
            Instant firstObservedAt,
            Instant lastObservedAt
    ) {
        LearnerInsight learnerInsight = LearnerInsight.builder()
                .learnerInsightId(requirePositive(learnerInsightId))
                .user(requireNonNull(user))
                .insightType(requireNonNull(insightType))
                .content(validateContent(content))
                .confidence(requireNonNull(confidence))
                .status(requireNonNull(status))
                .firstObservedAt(requireNonNull(firstObservedAt))
                .lastObservedAt(requireNonNull(lastObservedAt))
                .build();

        learnerInsight.validateObservedAt();
        learnerInsight.validateStatusAndConfidence();

        return learnerInsight;
    }

    public static LearnerInsight create(
            User user,
            InsightType insightType,
            String content,
            Instant observedAt
    ) {
        Instant validatedObservedAt = requireNonNull(observedAt);

        return LearnerInsight.builder()
                .user(requireNonNull(user))
                .insightType(requireNonNull(insightType))
                .content(validateContent(content))
                .confidence(LOW)
                .status(CANDIDATE)
                .firstObservedAt(validatedObservedAt)
                .lastObservedAt(validatedObservedAt)
                .build();
    }

    void recordEvidence(Instant observedAt) {
        if (this.status == SUPERSEDED) {
            throw new BusinessRuleViolationException(LEARNER_INSIGHT_EVIDENCE_ADDITION_NOT_ALLOWED);
        }

        Instant validatedObservedAt = requireNonNull(observedAt);

        if (validatedObservedAt.isBefore(this.lastObservedAt)) {
            throw new BusinessRuleViolationException(INVALID_LEARNER_INSIGHT_OBSERVATION_PERIOD);
        }

        this.lastObservedAt = validatedObservedAt;
    }

    public void reinforce() {
        validateChangeAllowed();

        this.confidence = switch (this.confidence) {
            case LOW -> MEDIUM;
            case MEDIUM, HIGH -> HIGH;
        };

        if (this.status == CANDIDATE) {
            activate();
        }
    }

    public void contradict() {
        validateChangeAllowed();

        this.confidence = switch (this.confidence) {
            case HIGH -> MEDIUM;
            case MEDIUM, LOW -> LOW;
        };

        if (this.status == ACTIVE && this.confidence == LOW) {
            reconsider();
        }
    }

    public void activate() {
        if (this.status != CANDIDATE || this.confidence == LOW) {
            throw new BusinessRuleViolationException(INVALID_LEARNER_INSIGHT_STATUS_TRANSITION);
        }

        this.status = ACTIVE;
    }

    public void reconsider() {
        if (this.status != ACTIVE || this.confidence != LOW) {
            throw new BusinessRuleViolationException(INVALID_LEARNER_INSIGHT_STATUS_TRANSITION);
        }

        this.status = CANDIDATE;
    }

    public void supersede() {
        validateChangeAllowed();

        this.status = SUPERSEDED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LearnerInsight that = (LearnerInsight) o;

        return this.learnerInsightId != null
                && Objects.equals(this.learnerInsightId, that.learnerInsightId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private void validateObservedAt() {
        if (this.lastObservedAt.isBefore(this.firstObservedAt)) {
            throw new BusinessRuleViolationException(INVALID_LEARNER_INSIGHT_OBSERVATION_PERIOD);
        }
    }

    private void validateStatusAndConfidence() {
        if (this.status == CANDIDATE && this.confidence != LOW) {
            throw new BusinessRuleViolationException(INVALID_LEARNER_INSIGHT_STATUS_AND_CONFIDENCE);
        }
        if (this.status == ACTIVE && this.confidence == LOW) {
            throw new BusinessRuleViolationException(INVALID_LEARNER_INSIGHT_STATUS_AND_CONFIDENCE);
        }
    }

    private void validateChangeAllowed() {
        if (this.status == SUPERSEDED) {
            throw new BusinessRuleViolationException(INVALID_LEARNER_INSIGHT_STATUS_TRANSITION);
        }
    }

    private static String validateContent(String content) {
        return requireMaxLength(requireNotBlank(content), INSIGHT_CONTENT_MAX_LENGTH);
    }
}
