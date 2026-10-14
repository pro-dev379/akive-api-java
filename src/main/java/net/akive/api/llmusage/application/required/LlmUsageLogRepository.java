package net.akive.api.llmusage.application.required;

import net.akive.api.llmusage.domain.LlmUsageLog;
import net.akive.api.llmusage.domain.LlmUsagePurpose;
import net.akive.api.llmusage.domain.LlmUsageScope;

import java.time.Instant;
import java.util.Collection;

public interface LlmUsageLogRepository {

    LlmUsageLog save(LlmUsageLog llmUsageLog);

    long sumTokenCountByUserIdAndUsageScopeAndPurposesAndCreatedAtRange(
            Long userId,
            LlmUsageScope usageScope,
            Collection<LlmUsagePurpose> purposes,
            Instant createdAtFrom,
            Instant createdAtBefore
    );
}
