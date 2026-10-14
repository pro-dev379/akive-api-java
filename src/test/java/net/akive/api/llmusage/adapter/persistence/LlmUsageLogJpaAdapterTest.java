package net.akive.api.llmusage.adapter.persistence;

import net.akive.api.llmusage.application.required.LlmUsageLogRepository;
import net.akive.api.llmusage.domain.LlmUsageLog;
import net.akive.api.llmusage.domain.LlmUsagePurpose;
import net.akive.api.llmusage.domain.LlmUsageScope;
import net.akive.api.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Set;

import static net.akive.api.llmusage.domain.LlmProvider.OPENAI;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.*;
import static net.akive.api.llmusage.domain.LlmUsageResultStatus.COMPLETED;
import static net.akive.api.llmusage.domain.LlmUsageScope.BATCH;
import static net.akive.api.llmusage.domain.LlmUsageScope.USER;
import static net.akive.api.llmusage.domain.fixture.LlmUsageLogFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.ANOTHER_USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LlmUsageLogJpaAdapterTest extends RepositoryTestSupport {

    private static final Instant CREATED_AT_FROM = Instant.parse("2026-10-17T00:00:00Z");
    private static final Instant CREATED_AT_BEFORE = Instant.parse("2026-10-18T00:00:00Z");

    @Autowired
    LlmUsageLogRepository llmUsageLogRepository;

    @Test
    void LLM_토큰_사용_로그를_저장한다() {
        LlmUsageLog llmUsageLog = LlmUsageLog.create(
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
                REASONING_OUTPUT_TOKEN_COUNT
        );

        LlmUsageLog savedLlmUsageLog = llmUsageLogRepository.save(llmUsageLog);

        assertThat(savedLlmUsageLog.getLlmUsageLogId()).isNotNull();
        assertThat(savedLlmUsageLog.getUserId()).isEqualTo(USER_ID);
        assertThat(savedLlmUsageLog.getPurpose()).isEqualTo(COACHING_RESPONSE);
        assertThat(savedLlmUsageLog.getProvider()).isEqualTo(OPENAI);
        assertThat(savedLlmUsageLog.getProviderResponseId()).isEqualTo(PROVIDER_RESPONSE_ID);
        assertThat(savedLlmUsageLog.getModelName()).isEqualTo(MODEL_NAME);
        assertThat(savedLlmUsageLog.getUsageScope()).isEqualTo(USER);
        assertThat(savedLlmUsageLog.getResultStatus()).isEqualTo(COMPLETED);
        assertThat(savedLlmUsageLog.getInputTokenCount()).isEqualTo(INPUT_TOKEN_COUNT);
        assertThat(savedLlmUsageLog.getCachedInputTokenCount()).isEqualTo(CACHED_INPUT_TOKEN_COUNT);
        assertThat(savedLlmUsageLog.getOutputTokenCount()).isEqualTo(OUTPUT_TOKEN_COUNT);
        assertThat(savedLlmUsageLog.getReasoningOutputTokenCount()).isEqualTo(REASONING_OUTPUT_TOKEN_COUNT);
        assertThat(savedLlmUsageLog.getCreatedAt()).isNotNull();
    }

    @Test
    void 같은_LLM_제공자와_응답_식별자를_중복으로_저장할_수_없다() {
        LlmUsageLog firstLlmUsageLog = createLlmUsageLog(
                USER_ID,
                COACHING_TITLE,
                OPENAI,
                PROVIDER_RESPONSE_ID,
                "gpt-5-nano",
                USER
        );
        LlmUsageLog anotherLlmUsageLog = createLlmUsageLog(
                ANOTHER_USER_ID,
                COACHING_TITLE,
                OPENAI,
                PROVIDER_RESPONSE_ID,
                "gpt-5-nano",
                USER
        );
        llmUsageLogRepository.save(firstLlmUsageLog);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> {
                    llmUsageLogRepository.save(anotherLlmUsageLog);
                    flushAndClear();
                });
    }

    @Test
    void 회원이_조회_기간_내_사용한_입력과_출력_토큰_수를_합산한다() {
        long firstInputTokenCount = 1_000L;
        long firstOutputTokenCount = 500L;
        long secondInputTokenCount = 2_000L;
        long secondOutputTokenCount = 700L;
        saveLlmUsageLog(
                USER_ID,
                COACHING_RESPONSE,
                USER,
                firstInputTokenCount,
                firstOutputTokenCount,
                CREATED_AT_FROM
        );
        saveLlmUsageLog(
                USER_ID,
                COACHING_TITLE,
                USER,
                secondInputTokenCount,
                secondOutputTokenCount,
                CREATED_AT_BEFORE.minusMillis(1)
        );
        flushAndClear();

        long totalTokenCount = llmUsageLogRepository
                .sumTokenCountByUserIdAndUsageScopeAndPurposesAndCreatedAtRange(
                        USER_ID,
                        USER,
                        Set.of(COACHING_RESPONSE, COACHING_TITLE),
                        CREATED_AT_FROM,
                        CREATED_AT_BEFORE
                );

        assertThat(totalTokenCount).isEqualTo(
                firstInputTokenCount
                        + firstOutputTokenCount
                        + secondInputTokenCount
                        + secondOutputTokenCount
        );
    }

    @Test
    void 조회_조건에_맞는_LLM_토큰_사용_로그만_합산한다() {
        long expectedInputTokenCount = 1_000L;
        long expectedOutputTokenCount = 500L;
        saveLlmUsageLog(
                USER_ID,
                COACHING_RESPONSE,
                USER,
                expectedInputTokenCount,
                expectedOutputTokenCount,
                CREATED_AT_FROM
        );
        saveLlmUsageLog(ANOTHER_USER_ID, COACHING_RESPONSE, USER, 100L, 100L, CREATED_AT_FROM);
        saveLlmUsageLog(USER_ID, COACHING_RESPONSE, BATCH, 100L, 100L, CREATED_AT_FROM);
        saveLlmUsageLog(USER_ID, LEARNER_INSIGHT_EMBEDDING, USER, 100L, 100L, CREATED_AT_FROM);
        saveLlmUsageLog(USER_ID, COACHING_RESPONSE, USER, 100L, 100L, CREATED_AT_FROM.minusMillis(1));
        saveLlmUsageLog(USER_ID, COACHING_RESPONSE, USER, 100L, 100L, CREATED_AT_BEFORE);
        flushAndClear();

        long totalTokenCount = llmUsageLogRepository
                .sumTokenCountByUserIdAndUsageScopeAndPurposesAndCreatedAtRange(
                        USER_ID,
                        USER,
                        Set.of(COACHING_RESPONSE),
                        CREATED_AT_FROM,
                        CREATED_AT_BEFORE
                );

        assertThat(totalTokenCount).isEqualTo(expectedInputTokenCount + expectedOutputTokenCount);
    }

    @Test
    void 조회_조건에_맞는_LLM_토큰_사용_로그가_없으면_사용량은_0이다() {
        long totalTokenCount = llmUsageLogRepository
                .sumTokenCountByUserIdAndUsageScopeAndPurposesAndCreatedAtRange(
                        USER_ID,
                        USER,
                        Set.of(COACHING_RESPONSE),
                        CREATED_AT_FROM,
                        CREATED_AT_BEFORE
                );

        assertThat(totalTokenCount).isZero();
    }

    private void saveLlmUsageLog(
            Long userId,
            LlmUsagePurpose purpose,
            LlmUsageScope usageScope,
            long inputTokenCount,
            long outputTokenCount,
            Instant createdAt
    ) {
        LlmUsageLog savedLlmUsageLog = llmUsageLogRepository.save(LlmUsageLog.create(
                userId,
                purpose,
                OPENAI,
                null,
                MODEL_NAME,
                usageScope,
                COMPLETED,
                inputTokenCount,
                CACHED_INPUT_TOKEN_COUNT,
                outputTokenCount,
                REASONING_OUTPUT_TOKEN_COUNT
        ));
        entityManager.flush();

        entityManager.createQuery("""
                    update LlmUsageLogEntity llmUsageLog
                    set llmUsageLog.createdAt = :createdAt
                    where llmUsageLog.llmUsageLogId = :llmUsageLogId
                """)
                .setParameter("createdAt", createdAt)
                .setParameter("llmUsageLogId", savedLlmUsageLog.getLlmUsageLogId())
                .executeUpdate();
    }
}
