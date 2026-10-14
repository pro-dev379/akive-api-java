package net.akive.api.llmusage.application.provided.dto;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.llmusage.domain.LlmProvider;
import net.akive.api.llmusage.domain.LlmUsagePurpose;
import net.akive.api.llmusage.domain.LlmUsageResultStatus;
import net.akive.api.llmusage.domain.LlmUsageScope;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.LLM_USAGE_USER_ID_REQUIRED;
import static net.akive.api.llmusage.domain.LlmUsagePolicy.MODEL_NAME_MAX_LENGTH;
import static net.akive.api.llmusage.domain.LlmUsageScope.USER;

public record LlmUsageRecordCommand(
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

    public LlmUsageRecordCommand {
        requirePositiveIfPresent(userId);

        requireNonNull(purpose);
        requireNonNull(provider);
        requireNonNull(usageScope);
        requireNonNull(resultStatus);

        if (providerResponseId != null) {
            providerResponseId = requireNotBlank(providerResponseId);
        }

        modelName = requireMaxLength(requireNotBlank(modelName), MODEL_NAME_MAX_LENGTH);

        requireNonNegative(inputTokenCount);
        requireNonNegative(cachedInputTokenCount);
        requireNonNegative(outputTokenCount);
        requireNonNegative(reasoningOutputTokenCount);

        if (usageScope == USER && userId == null) {
            throw new BusinessRuleViolationException(LLM_USAGE_USER_ID_REQUIRED);
        }
    }
}
