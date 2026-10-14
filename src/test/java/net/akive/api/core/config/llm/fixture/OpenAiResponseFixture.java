package net.akive.api.core.config.llm.fixture;

import com.openai.models.responses.ResponseUsage;

public final class OpenAiResponseFixture {

    public static final long INPUT_TOKEN_COUNT = 100L;
    public static final long CACHED_INPUT_TOKEN_COUNT = 20L;
    public static final long OUTPUT_TOKEN_COUNT = 30L;
    public static final long REASONING_OUTPUT_TOKEN_COUNT = 10L;

    private OpenAiResponseFixture() {
    }

    public static ResponseUsage createResponseUsage() {
        return ResponseUsage.builder()
                .inputTokens(INPUT_TOKEN_COUNT)
                .inputTokensDetails(ResponseUsage.InputTokensDetails.builder()
                        .cacheWriteTokens(0L)
                        .cachedTokens(CACHED_INPUT_TOKEN_COUNT)
                        .build())
                .outputTokens(OUTPUT_TOKEN_COUNT)
                .outputTokensDetails(ResponseUsage.OutputTokensDetails.builder()
                        .reasoningTokens(REASONING_OUTPUT_TOKEN_COUNT)
                        .build())
                .totalTokens(INPUT_TOKEN_COUNT + OUTPUT_TOKEN_COUNT)
                .build();
    }
}
