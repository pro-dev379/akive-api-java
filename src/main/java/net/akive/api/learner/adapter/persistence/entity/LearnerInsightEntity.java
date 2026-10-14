package net.akive.api.learner.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.learner.domain.InsightConfidence;
import net.akive.api.learner.domain.InsightStatus;
import net.akive.api.learner.domain.InsightType;
import net.akive.api.learner.domain.LearnerInsight;
import net.akive.api.user.adapter.persistence.entity.UserEntity;

import java.time.Instant;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "learner_insight")
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class LearnerInsightEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long learnerInsightId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private InsightType insightType;

    @Column(nullable = false, length = 500)
    private String content;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private InsightConfidence confidence;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private InsightStatus status;

    @Column(nullable = false, updatable = false)
    private Instant firstObservedAt;

    @Column(nullable = false)
    private Instant lastObservedAt;

    public static LearnerInsightEntity from(LearnerInsight learnerInsight) {
        return new LearnerInsightEntity(
                learnerInsight.getLearnerInsightId(),
                UserEntity.from(learnerInsight.getUser()),
                learnerInsight.getInsightType(),
                learnerInsight.getContent(),
                learnerInsight.getConfidence(),
                learnerInsight.getStatus(),
                learnerInsight.getFirstObservedAt(),
                learnerInsight.getLastObservedAt()
        );
    }

    public LearnerInsight to() {
        try {
            return LearnerInsight.reconstruct(
                    this.learnerInsightId,
                    this.user.to(),
                    this.insightType,
                    this.content,
                    this.confidence,
                    this.status,
                    this.firstObservedAt,
                    this.lastObservedAt
            );
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException(
                    "Failed to reconstruct LearnerInsight: " + this.learnerInsightId,
                    e
            );
        }
    }
}
