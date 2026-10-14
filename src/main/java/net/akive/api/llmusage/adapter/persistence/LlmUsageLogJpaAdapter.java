package net.akive.api.llmusage.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.llmusage.adapter.persistence.entity.LlmUsageLogEntity;
import net.akive.api.llmusage.application.required.LlmUsageLogRepository;
import net.akive.api.llmusage.domain.LlmUsageLog;
import net.akive.api.llmusage.domain.LlmUsagePurpose;
import net.akive.api.llmusage.domain.LlmUsageScope;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;

@Repository
@RequiredArgsConstructor
class LlmUsageLogJpaAdapter implements LlmUsageLogRepository {

    private final LlmUsageLogJpaRepository llmUsageLogJpaRepository;

    @Override
    public LlmUsageLog save(LlmUsageLog llmUsageLog) {
        return llmUsageLogJpaRepository.save(LlmUsageLogEntity.from(llmUsageLog)).to();
    }

    @Override
    public long sumTokenCountByUserIdAndUsageScopeAndPurposesAndCreatedAtRange(
            Long userId,
            LlmUsageScope usageScope,
            Collection<LlmUsagePurpose> purposes,
            Instant createdAtFrom,
            Instant createdAtBefore
    ) {
        return llmUsageLogJpaRepository.sumTokenCountByUserIdAndUsageScopeAndPurposesAndCreatedAtRange(
                        userId,
                        usageScope,
                        purposes,
                        createdAtFrom,
                        createdAtBefore
                );
    }
}
