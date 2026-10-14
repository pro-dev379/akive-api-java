package net.akive.api.llmusage.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.Instant;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.LLM_USAGE_USER_ID_REQUIRED;
import static net.akive.api.llmusage.domain.LlmUsagePolicy.MODEL_NAME_MAX_LENGTH;
import static net.akive.api.llmusage.domain.LlmUsageScope.USER;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class LlmUsageLog {

    private Long llmUsageLogId;

    private Long userId;

    private LlmUsagePurpose purpose;

    private LlmProvider provider;

    private String providerResponseId;

    private String modelName;

    private LlmUsageScope usageScope;

    private LlmUsageResultStatus resultStatus;

    private long inputTokenCount;

    private long cachedInputTokenCount;

    private long outputTokenCount;

    private long reasoningOutputTokenCount;

    private Instant createdAt;

    public static LlmUsageLog reconstruct(
            Long llmUsageLogId,
            Long userId,
            LlmUsagePurpose purpose,
            LlmProvider provider,
            String providerResponseId,
            String modelName,
            LlmUsageScope usageScope,
            LlmUsageResultStatus resultStatus,
            long inputTokenCount,
            long cachedInputTokenCount,
            long outputTokenCount,
            long reasoningOutputTokenCount,
            Instant createdAt
    ) {
        LlmUsageLog llmUsageLog = LlmUsageLog.builder()
                .llmUsageLogId(requirePositive(llmUsageLogId))
                .userId(requirePositiveIfPresent(userId))
                .purpose(requireNonNull(purpose))
                .provider(requireNonNull(provider))
                .providerResponseId(validateProviderResponseId(providerResponseId))
                .modelName(requireMaxLength(requireNotBlank(modelName), MODEL_NAME_MAX_LENGTH))
                .usageScope(requireNonNull(usageScope))
                .resultStatus(requireNonNull(resultStatus))
                .inputTokenCount(requireNonNegative(inputTokenCount))
                .cachedInputTokenCount(requireNonNegative(cachedInputTokenCount))
                .outputTokenCount(requireNonNegative(outputTokenCount))
                .reasoningOutputTokenCount(requireNonNegative(reasoningOutputTokenCount))
                .createdAt(requireNonNull(createdAt))
                .build();

        llmUsageLog.validateUserScope();

        return llmUsageLog;
    }

    public static LlmUsageLog create(
            Long userId,
            LlmUsagePurpose purpose,
            LlmProvider provider,
            String providerResponseId,
            String modelName,
            LlmUsageScope usageScope,
            LlmUsageResultStatus resultStatus,
            long inputTokenCount,
            long cachedInputTokenCount,
            long outputTokenCount,
            long reasoningOutputTokenCount
    ) {
        LlmUsageLog llmUsageLog = LlmUsageLog.builder()
                .userId(requirePositiveIfPresent(userId))
                .purpose(requireNonNull(purpose))
                .provider(requireNonNull(provider))
                .providerResponseId(validateProviderResponseId(providerResponseId))
                .modelName(requireMaxLength(requireNotBlank(modelName), MODEL_NAME_MAX_LENGTH))
                .usageScope(requireNonNull(usageScope))
                .resultStatus(requireNonNull(resultStatus))
                .inputTokenCount(requireNonNegative(inputTokenCount))
                .cachedInputTokenCount(requireNonNegative(cachedInputTokenCount))
                .outputTokenCount(requireNonNegative(outputTokenCount))
                .reasoningOutputTokenCount(requireNonNegative(reasoningOutputTokenCount))
                .build();

        llmUsageLog.validateUserScope();

        return llmUsageLog;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LlmUsageLog that = (LlmUsageLog) o;

        return this.llmUsageLogId != null
                && Objects.equals(this.llmUsageLogId, that.llmUsageLogId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private void validateUserScope() {
        if (this.usageScope == USER && this.userId == null) {
            throw new BusinessRuleViolationException(LLM_USAGE_USER_ID_REQUIRED);
        }
    }

    private static String validateProviderResponseId(String providerResponseId) {
        if (providerResponseId == null) {
            return null;
        }

        return requireNotBlank(providerResponseId);
    }
}
