package net.akive.api.core.config.llm.fixture;

import net.akive.api.core.config.llm.OpenAiProperties;

import java.time.Duration;

public final class OpenAiPropertiesFixture {

    public static final String OPENAI_API_KEY = "openai-api-key";
    public static final Duration OPENAI_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration OPENAI_STREAMING_TIMEOUT = Duration.ofMinutes(5);
    public static final String COACHING_MODEL = "gpt-5-mini";
    public static final String CONVERSATION_TITLE_MODEL = "gpt-5-nano";
    public static final String LEARNER_INSIGHT_MODEL = "gpt-5-mini";
    public static final String EMBEDDING_MODEL = "text-embedding-3-small";
    public static final int EMBEDDING_DIMENSIONS = 1536;

    private OpenAiPropertiesFixture() {
    }

    public static OpenAiProperties createOpenAiProperties() {
        return new OpenAiProperties(
                OPENAI_API_KEY,
                OPENAI_TIMEOUT,
                OPENAI_STREAMING_TIMEOUT,
                new OpenAiProperties.Model(
                        COACHING_MODEL,
                        CONVERSATION_TITLE_MODEL,
                        LEARNER_INSIGHT_MODEL,
                        EMBEDDING_MODEL
                ),
                new OpenAiProperties.Embedding(EMBEDDING_DIMENSIONS)
        );
    }
}
