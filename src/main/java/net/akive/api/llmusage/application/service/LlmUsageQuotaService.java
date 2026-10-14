package net.akive.api.llmusage.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.RateLimitExceededException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.llmusage.application.provided.LlmUsageQuotaChecker;
import net.akive.api.llmusage.application.required.LlmUsageLogRepository;
import net.akive.api.llmusage.domain.LlmUsagePurpose;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import static net.akive.api.common.exception.business.RateLimitError.LLM_DAILY_TOKEN_QUOTA_EXCEEDED;
import static net.akive.api.common.time.ServiceDayPolicy.toInstantAtServiceZone;
import static net.akive.api.common.time.ServiceDayPolicy.toKoreanDate;
import static net.akive.api.llmusage.domain.LlmUsagePolicy.USER_DAILY_TOKEN_MAX_COUNT;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.COACHING_RESPONSE;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.COACHING_TITLE;
import static net.akive.api.llmusage.domain.LlmUsageScope.USER;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class LlmUsageQuotaService implements LlmUsageQuotaChecker {

    private static final Set<LlmUsagePurpose> USER_QUOTA_PURPOSES = Set.of(COACHING_RESPONSE, COACHING_TITLE);

    private final LlmUsageLogRepository llmUsageLogRepository;
    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public void checkUserDailyQuota(Long userId) throws RateLimitExceededException {
        LocalDate currentDate = toKoreanDate(currentTimeProvider.now());
        Instant createdAtFrom = toInstantAtServiceZone(currentDate.atStartOfDay());
        Instant createdAtBefore = toInstantAtServiceZone(currentDate.plusDays(1).atStartOfDay());

        long usedTokenCount = llmUsageLogRepository
                .sumTokenCountByUserIdAndUsageScopeAndPurposesAndCreatedAtRange(
                        userId,
                        USER,
                        USER_QUOTA_PURPOSES,
                        createdAtFrom,
                        createdAtBefore
                );

        if (usedTokenCount >= USER_DAILY_TOKEN_MAX_COUNT) {
            throw new RateLimitExceededException(LLM_DAILY_TOKEN_QUOTA_EXCEEDED);
        }
    }
}
