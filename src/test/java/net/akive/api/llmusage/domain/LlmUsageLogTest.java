package net.akive.api.llmusage.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.LLM_USAGE_USER_ID_REQUIRED;
import static net.akive.api.llmusage.domain.LlmProvider.OPENAI;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.COACHING_RESPONSE;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.LEARNER_INSIGHT_SEARCH_EMBEDDING;
import static net.akive.api.llmusage.domain.LlmUsageResultStatus.COMPLETED;
import static net.akive.api.llmusage.domain.LlmUsageScope.BATCH;
import static net.akive.api.llmusage.domain.LlmUsageScope.USER;
import static net.akive.api.llmusage.domain.fixture.LlmUsageLogFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LlmUsageLogTest {

    @Test
    void 저장된_LLM_토큰_사용_로그를_복원한다() {
        LlmUsageLog llmUsageLog = LlmUsageLog.reconstruct(
                LLM_USAGE_LOG_ID,
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

        assertThat(llmUsageLog.getLlmUsageLogId()).isEqualTo(LLM_USAGE_LOG_ID);
        assertThat(llmUsageLog.getUserId()).isEqualTo(USER_ID);
        assertThat(llmUsageLog.getPurpose()).isEqualTo(COACHING_RESPONSE);
        assertThat(llmUsageLog.getProvider()).isEqualTo(OPENAI);
        assertThat(llmUsageLog.getProviderResponseId()).isEqualTo(PROVIDER_RESPONSE_ID);
        assertThat(llmUsageLog.getModelName()).isEqualTo(MODEL_NAME);
        assertThat(llmUsageLog.getUsageScope()).isEqualTo(USER);
        assertThat(llmUsageLog.getResultStatus()).isEqualTo(COMPLETED);
        assertThat(llmUsageLog.getInputTokenCount()).isEqualTo(INPUT_TOKEN_COUNT);
        assertThat(llmUsageLog.getCachedInputTokenCount()).isEqualTo(CACHED_INPUT_TOKEN_COUNT);
        assertThat(llmUsageLog.getOutputTokenCount()).isEqualTo(OUTPUT_TOKEN_COUNT);
        assertThat(llmUsageLog.getReasoningOutputTokenCount()).isEqualTo(REASONING_OUTPUT_TOKEN_COUNT);
        assertThat(llmUsageLog.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void 회원의_LLM_토큰_사용_로그를_생성한다() {
        LlmUsageLog llmUsageLog = LlmUsageLog.create(
                USER_ID,
                COACHING_RESPONSE,
                OPENAI,
                "  " + PROVIDER_RESPONSE_ID + "  ",
                "  " + MODEL_NAME + "  ",
                USER,
                COMPLETED,
                INPUT_TOKEN_COUNT,
                CACHED_INPUT_TOKEN_COUNT,
                OUTPUT_TOKEN_COUNT,
                REASONING_OUTPUT_TOKEN_COUNT
        );

        assertThat(llmUsageLog.getLlmUsageLogId()).isNull();
        assertThat(llmUsageLog.getUserId()).isEqualTo(USER_ID);
        assertThat(llmUsageLog.getProviderResponseId()).isEqualTo(PROVIDER_RESPONSE_ID);
        assertThat(llmUsageLog.getModelName()).isEqualTo(MODEL_NAME);
        assertThat(llmUsageLog.getUsageScope()).isEqualTo(USER);
        assertThat(llmUsageLog.getInputTokenCount()).isEqualTo(INPUT_TOKEN_COUNT);
        assertThat(llmUsageLog.getCachedInputTokenCount()).isEqualTo(CACHED_INPUT_TOKEN_COUNT);
        assertThat(llmUsageLog.getOutputTokenCount()).isEqualTo(OUTPUT_TOKEN_COUNT);
        assertThat(llmUsageLog.getReasoningOutputTokenCount()).isEqualTo(REASONING_OUTPUT_TOKEN_COUNT);
        assertThat(llmUsageLog.getCreatedAt()).isNull();
    }

    @Test
    void 회원의_LLM_토큰_사용_로그는_회원_식별자_없이_생성될_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> createLlmUsageLog(null, USER, MODEL_NAME, PROVIDER_RESPONSE_ID))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(LLM_USAGE_USER_ID_REQUIRED);
    }

    @Test
    void 배치의_LLM_토큰_사용_로그는_회원_식별자_없이_생성될_수_있다() {
        LlmUsageLog llmUsageLog = createLlmUsageLog(
                null,
                BATCH,
                MODEL_NAME,
                PROVIDER_RESPONSE_ID
        );

        assertThat(llmUsageLog.getUserId()).isNull();
        assertThat(llmUsageLog.getProviderResponseId()).isEqualTo(PROVIDER_RESPONSE_ID);
        assertThat(llmUsageLog.getUsageScope()).isEqualTo(BATCH);
    }

    @Test
    void LLM_제공자_응답_식별자가_없는_LLM_토큰_사용_로그를_생성할_수_있다() {
        LlmUsageLog llmUsageLog = createLlmUsageLog(
                USER_ID,
                LEARNER_INSIGHT_SEARCH_EMBEDDING,
                USER,
                MODEL_NAME,
                null
        );

        assertThat(llmUsageLog.getUserId()).isEqualTo(USER_ID);
        assertThat(llmUsageLog.getPurpose()).isEqualTo(LEARNER_INSIGHT_SEARCH_EMBEDDING);
        assertThat(llmUsageLog.getProviderResponseId()).isNull();
        assertThat(llmUsageLog.getUsageScope()).isEqualTo(USER);
    }

    @Test
    void 같은_식별자를_가진_LLM_토큰_사용_로그는_같게_취급한다() {
        LlmUsageLog llmUsageLog = reconstructLlmUsageLog();
        LlmUsageLog sameLlmUsageLog = reconstructLlmUsageLog(LLM_USAGE_LOG_ID);

        assertThat(llmUsageLog).isEqualTo(sameLlmUsageLog);
        assertThat(llmUsageLog.hashCode()).isEqualTo(sameLlmUsageLog.hashCode());
    }

    @Test
    void 식별자가_다른_LLM_토큰_사용_로그는_다르게_취급한다() {
        LlmUsageLog llmUsageLog = reconstructLlmUsageLog();
        LlmUsageLog anotherLlmUsageLog = reconstructLlmUsageLog(ANOTHER_LLM_USAGE_LOG_ID);

        assertThat(llmUsageLog).isNotEqualTo(anotherLlmUsageLog);
    }

    @Test
    void 식별자가_없는_LLM_토큰_사용_로그끼리는_나머지_값이_같더라도_다르게_취급한다() {
        LlmUsageLog llmUsageLog = createLlmUsageLog(USER_ID, USER, MODEL_NAME, PROVIDER_RESPONSE_ID);
        LlmUsageLog anotherLlmUsageLog = createLlmUsageLog(USER_ID, USER, MODEL_NAME, PROVIDER_RESPONSE_ID);

        assertThat(llmUsageLog).isNotEqualTo(anotherLlmUsageLog);
    }
}
