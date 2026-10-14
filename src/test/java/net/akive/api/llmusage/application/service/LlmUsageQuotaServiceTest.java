package net.akive.api.llmusage.application.service;

import net.akive.api.common.exception.business.RateLimitExceededException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.llmusage.application.required.LlmUsageLogRepository;
import net.akive.api.llmusage.domain.LlmUsagePurpose;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Set;

import static net.akive.api.common.exception.business.RateLimitError.LLM_DAILY_TOKEN_QUOTA_EXCEEDED;
import static net.akive.api.llmusage.domain.LlmUsagePolicy.USER_DAILY_TOKEN_MAX_COUNT;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.COACHING_RESPONSE;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.COACHING_TITLE;
import static net.akive.api.llmusage.domain.LlmUsageScope.USER;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LlmUsageQuotaServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-15T05:00:00Z");
    private static final Instant TODAY_START = Instant.parse("2026-10-14T15:00:00Z");
    private static final Instant TOMORROW_START = Instant.parse("2026-10-15T15:00:00Z");
    private static final Set<LlmUsagePurpose> USER_QUOTA_PURPOSES = Set.of(COACHING_RESPONSE, COACHING_TITLE);

    @Mock
    LlmUsageLogRepository llmUsageLogRepository;

    @Mock
    CurrentTimeProvider currentTimeProvider;

    @InjectMocks
    LlmUsageQuotaService llmUsageQuotaService;

    @Test
    void 한국_시간_기준_오늘_LLM_토큰_사용량이_제한_사용량보다_적으면_LLM_사용을_허용한다() {
        given(currentTimeProvider.now()).willReturn(NOW);
        given(llmUsageLogRepository.sumTokenCountByUserIdAndUsageScopeAndPurposesAndCreatedAtRange(
                USER_ID,
                USER,
                USER_QUOTA_PURPOSES,
                TODAY_START,
                TOMORROW_START
        )).willReturn(USER_DAILY_TOKEN_MAX_COUNT - 1);

        assertThatCode(() -> llmUsageQuotaService.checkUserDailyQuota(USER_ID))
                .doesNotThrowAnyException();
    }

    @Test
    void 한국_시간_기준_오늘_LLM_토큰_사용량이_제한_사용량에_도달하면_예외를_던진다() {
        given(currentTimeProvider.now()).willReturn(NOW);
        given(llmUsageLogRepository.sumTokenCountByUserIdAndUsageScopeAndPurposesAndCreatedAtRange(
                USER_ID,
                USER,
                USER_QUOTA_PURPOSES,
                TODAY_START,
                TOMORROW_START
        )).willReturn(USER_DAILY_TOKEN_MAX_COUNT);

        assertThatExceptionOfType(RateLimitExceededException.class)
                .isThrownBy(() -> llmUsageQuotaService.checkUserDailyQuota(USER_ID))
                .extracting(RateLimitExceededException::getError)
                .isEqualTo(LLM_DAILY_TOKEN_QUOTA_EXCEEDED);
    }

    @Test
    void 한국_시간_기준_오늘_LLM_토큰_사용량이_제한_사용량을_초과하면_예외를_던진다() {
        given(currentTimeProvider.now()).willReturn(NOW);
        given(llmUsageLogRepository.sumTokenCountByUserIdAndUsageScopeAndPurposesAndCreatedAtRange(
                USER_ID,
                USER,
                USER_QUOTA_PURPOSES,
                TODAY_START,
                TOMORROW_START
        )).willReturn(USER_DAILY_TOKEN_MAX_COUNT + 1);

        assertThatExceptionOfType(RateLimitExceededException.class)
                .isThrownBy(() -> llmUsageQuotaService.checkUserDailyQuota(USER_ID))
                .extracting(RateLimitExceededException::getError)
                .isEqualTo(LLM_DAILY_TOKEN_QUOTA_EXCEEDED);
    }
}
