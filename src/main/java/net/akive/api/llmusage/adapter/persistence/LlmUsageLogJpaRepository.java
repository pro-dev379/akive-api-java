package net.akive.api.llmusage.adapter.persistence;

import net.akive.api.llmusage.adapter.persistence.entity.LlmUsageLogEntity;
import net.akive.api.llmusage.domain.LlmUsagePurpose;
import net.akive.api.llmusage.domain.LlmUsageScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;

interface LlmUsageLogJpaRepository extends JpaRepository<LlmUsageLogEntity, Long> {

    @Query("""
            select coalesce(sum(llmUsageLog.inputTokenCount + llmUsageLog.outputTokenCount), 0)
            from LlmUsageLogEntity llmUsageLog
            where llmUsageLog.userId = :userId
              and llmUsageLog.usageScope = :usageScope
              and llmUsageLog.purpose in :purposes
              and llmUsageLog.createdAt >= :createdAtFrom
              and llmUsageLog.createdAt < :createdAtBefore
            """)
    long sumTokenCountByUserIdAndUsageScopeAndPurposesAndCreatedAtRange(
            Long userId,
            LlmUsageScope usageScope,
            Collection<LlmUsagePurpose> purposes,
            Instant createdAtFrom,
            Instant createdAtBefore
    );
}
