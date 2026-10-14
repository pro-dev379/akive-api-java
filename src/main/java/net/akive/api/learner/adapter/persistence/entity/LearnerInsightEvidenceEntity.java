package net.akive.api.learner.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.learner.domain.EvidenceSourceType;
import net.akive.api.learner.domain.EvidenceStance;
import net.akive.api.learner.domain.LearnerInsightEvidence;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "learner_insight_evidence",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-learner_insight_evidence-insight_id-source_type-source_id",
                        columnNames = {"learner_insight_id", "source_type", "source_id"}
                )
        }
)
@Getter
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class LearnerInsightEvidenceEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long learnerInsightEvidenceId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "learner_insight_id", nullable = false)
    private LearnerInsightEntity learnerInsight;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private EvidenceSourceType sourceType;

    @Column(nullable = false)
    private Long sourceId;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private EvidenceStance stance;

    @Column(nullable = false, length = 500)
    private String content;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static LearnerInsightEvidenceEntity from(LearnerInsightEvidence evidence) {
        return new LearnerInsightEvidenceEntity(
                evidence.getLearnerInsightEvidenceId(),
                LearnerInsightEntity.from(evidence.getLearnerInsight()),
                evidence.getSourceType(),
                evidence.getSourceId(),
                evidence.getStance(),
                evidence.getContent(),
                evidence.getCreatedAt()
        );
    }

    public LearnerInsightEvidence to() {
        try {
            return LearnerInsightEvidence.reconstruct(
                    this.learnerInsightEvidenceId,
                    this.learnerInsight.to(),
                    this.sourceType,
                    this.sourceId,
                    this.stance,
                    this.content,
                    this.createdAt
            );
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException(
                    "Failed to reconstruct LearnerInsightEvidence: " + this.learnerInsightEvidenceId,
                    e
            );
        }
    }
}
