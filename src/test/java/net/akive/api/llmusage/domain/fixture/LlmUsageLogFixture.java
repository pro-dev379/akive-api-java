package net.akive.api.llmusage.domain.fixture;

import net.akive.api.llmusage.domain.LlmUsageLog;
import net.akive.api.llmusage.domain.LlmProvider;
import net.akive.api.llmusage.domain.LlmUsagePurpose;
import net.akive.api.llmusage.domain.LlmUsageScope;

import java.time.Instant;

import static net.akive.api.llmusage.domain.LlmProvider.OPENAI;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.COACHING_RESPONSE;
import static net.akive.api.llmusage.domain.LlmUsageResultStatus.COMPLETED;
import static net.akive.api.llmusage.domain.LlmUsageScope.USER;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public final class LlmUsageLogFixture {

    public static final Long LLM_USAGE_LOG_ID = 1L;
    public static final Long ANOTHER_LLM_USAGE_LOG_ID = 2L;
    public static final String PROVIDER_RESPONSE_ID = "resp_0199a8e27c317abc8123456789abcdef";
    public static final String MODEL_NAME = "gpt-5-mini";
    public static final long INPUT_TOKEN_COUNT = 1_000L;
    public static final long CACHED_INPUT_TOKEN_COUNT = 200L;
    public static final long OUTPUT_TOKEN_COUNT = 500L;
    public static final long REASONING_OUTPUT_TOKEN_COUNT = 100L;
    public static final Instant CREATED_AT = Instant.parse("2026-10-17T00:00:00Z");

    private LlmUsageLogFixture() {
    }

    public static LlmUsageLog createLlmUsageLog() {
        return createLlmUsageLog(USER_ID, USER, MODEL_NAME, PROVIDER_RESPONSE_ID);
    }

    public static LlmUsageLog createLlmUsageLog(
            Long userId,
            LlmUsageScope usageScope,
            String modelName,
            String providerResponseId
    ) {
        return createLlmUsageLog(
                userId,
                COACHING_RESPONSE,
                usageScope,
                modelName,
                providerResponseId
        );
    }

    public static LlmUsageLog createLlmUsageLog(
            Long userId,
            LlmUsagePurpose purpose,
            LlmUsageScope usageScope,
            String modelName,
            String providerResponseId
    ) {
        return createLlmUsageLog(
                userId,
                purpose,
                OPENAI,
                providerResponseId,
                modelName,
                usageScope
        );
    }

    public static LlmUsageLog createLlmUsageLog(
            Long userId,
            LlmUsagePurpose purpose,
            LlmProvider provider,
            String providerResponseId,
            String modelName,
            LlmUsageScope usageScope
    ) {
        return LlmUsageLog.create(
                userId,
                purpose,
                provider,
                providerResponseId,
                modelName,
                usageScope,
                COMPLETED,
                INPUT_TOKEN_COUNT,
                CACHED_INPUT_TOKEN_COUNT,
                OUTPUT_TOKEN_COUNT,
                REASONING_OUTPUT_TOKEN_COUNT
        );
    }

    public static LlmUsageLog reconstructLlmUsageLog() {
        return reconstructLlmUsageLog(LLM_USAGE_LOG_ID);
    }

    public static LlmUsageLog reconstructLlmUsageLog(Long llmUsageLogId) {
        return LlmUsageLog.reconstruct(
                llmUsageLogId,
                USER_ID,
                COACHING_RESPONSE,
                OPENAI,
                PROVIDER_RESPONSE_ID,
                MODEL_NAME,
                USER,
                COMPLETED,
                INPUT_TOKEN_COUNT,
                CACHED_INPUT_TOKEN_COUNT,
                OUTPUT_TOKEN_COUNT,
                REASONING_OUTPUT_TOKEN_COUNT,
                CREATED_AT
        );
    }

}
