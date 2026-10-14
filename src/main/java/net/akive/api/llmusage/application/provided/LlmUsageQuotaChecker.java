package net.akive.api.llmusage.application.provided;

import net.akive.api.common.exception.business.RateLimitExceededException;

public interface LlmUsageQuotaChecker {

    void checkUserDailyQuota(Long userId) throws RateLimitExceededException;
}
