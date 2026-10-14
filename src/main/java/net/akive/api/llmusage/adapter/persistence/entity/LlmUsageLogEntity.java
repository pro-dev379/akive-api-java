package net.akive.api.llmusage.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.llmusage.domain.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "llm_usage_log",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-llm_usage_log-provider-provider_response_id",
                        columnNames = {"provider", "provider_response_id"}
                )
        }
)
@Getter
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class LlmUsageLogEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long llmUsageLogId;

    @Column(updatable = false)
    private Long userId;

    @Enumerated(STRING)
    @Column(nullable = false, updatable = false, columnDefinition = "VARCHAR(50)")
    private LlmUsagePurpose purpose;

    @Enumerated(STRING)
    @Column(nullable = false, updatable = false, columnDefinition = "VARCHAR(50)")
    private LlmProvider provider;

    @Column(updatable = false)
    private String providerResponseId;

    @Column(nullable = false, length = 100, updatable = false)
    private String modelName;

    @Enumerated(STRING)
    @Column(nullable = false, updatable = false, columnDefinition = "VARCHAR(50)")
    private LlmUsageScope usageScope;

    @Enumerated(STRING)
    @Column(nullable = false, updatable = false, columnDefinition = "VARCHAR(50)")
    private LlmUsageResultStatus resultStatus;

    @Column(nullable = false, updatable = false)
    private long inputTokenCount;

    @Column(nullable = false, updatable = false)
    private long cachedInputTokenCount;

    @Column(nullable = false, updatable = false)
    private long outputTokenCount;

    @Column(nullable = false, updatable = false)
    private long reasoningOutputTokenCount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static LlmUsageLogEntity from(LlmUsageLog llmUsageLog) {
        return new LlmUsageLogEntity(
                llmUsageLog.getLlmUsageLogId(),
                llmUsageLog.getUserId(),
                llmUsageLog.getPurpose(),
                llmUsageLog.getProvider(),
                llmUsageLog.getProviderResponseId(),
                llmUsageLog.getModelName(),
                llmUsageLog.getUsageScope(),
                llmUsageLog.getResultStatus(),
                llmUsageLog.getInputTokenCount(),
                llmUsageLog.getCachedInputTokenCount(),
                llmUsageLog.getOutputTokenCount(),
                llmUsageLog.getReasoningOutputTokenCount(),
                llmUsageLog.getCreatedAt()
        );
    }

    public LlmUsageLog to() {
        try {
            return LlmUsageLog.reconstruct(
                    this.llmUsageLogId,
                    this.userId,
                    this.purpose,
                    this.provider,
                    this.providerResponseId,
                    this.modelName,
                    this.usageScope,
                    this.resultStatus,
                    this.inputTokenCount,
                    this.cachedInputTokenCount,
                    this.outputTokenCount,
                    this.reasoningOutputTokenCount,
                    this.createdAt
            );
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException(
                    "Failed to reconstruct LlmUsageLog: " + this.llmUsageLogId,
                    e
            );
        }
    }
}
