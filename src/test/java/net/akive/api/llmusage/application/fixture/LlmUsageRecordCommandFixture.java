package net.akive.api.llmusage.application.fixture;

import net.akive.api.llmusage.application.provided.dto.LlmUsageRecordCommand;
import net.akive.api.llmusage.domain.LlmUsagePurpose;
import net.akive.api.llmusage.domain.LlmUsageResultStatus;
import net.akive.api.llmusage.domain.LlmUsageScope;

import static net.akive.api.core.config.llm.fixture.OpenAiResponseFixture.*;
import static net.akive.api.core.config.llm.fixture.OpenAiPropertiesFixture.EMBEDDING_MODEL;
import static net.akive.api.llmusage.domain.LlmProvider.OPENAI;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.COACHING_RESPONSE;
import static net.akive.api.llmusage.domain.LlmUsageResultStatus.COMPLETED;
import static net.akive.api.llmusage.domain.LlmUsageScope.USER;
import static net.akive.api.llmusage.domain.fixture.LlmUsageLogFixture.MODEL_NAME;
import static net.akive.api.llmusage.domain.fixture.LlmUsageLogFixture.PROVIDER_RESPONSE_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public final class LlmUsageRecordCommandFixture {

    private LlmUsageRecordCommandFixture() {
    }

    public static LlmUsageRecordCommand createLlmUsageRecordCommand() {
        return createLlmUsageRecordCommand(
                USER_ID,
                COACHING_RESPONSE,
                PROVIDER_RESPONSE_ID,
                MODEL_NAME,
                USER,
                COMPLETED
        );
    }

    public static LlmUsageRecordCommand createLlmUsageRecordCommand(
            Long userId,
            LlmUsagePurpose purpose,
            String providerResponseId,
            String modelName,
            LlmUsageScope usageScope,
            LlmUsageResultStatus resultStatus
    ) {
        return new LlmUsageRecordCommand(
                userId,
                purpose,
                OPENAI,
                providerResponseId,
                modelName,
                usageScope,
                resultStatus,
                INPUT_TOKEN_COUNT,
                CACHED_INPUT_TOKEN_COUNT,
                OUTPUT_TOKEN_COUNT,
                REASONING_OUTPUT_TOKEN_COUNT
        );
    }

    public static LlmUsageRecordCommand createEmbeddingUsageRecordCommand(
            Long userId,
            LlmUsagePurpose purpose,
            LlmUsageScope usageScope
    ) {
        return new LlmUsageRecordCommand(
                userId,
                purpose,
                OPENAI,
                null,
                EMBEDDING_MODEL,
                usageScope,
                COMPLETED,
                INPUT_TOKEN_COUNT,
                0,
                0,
                0
        );
    }
}
