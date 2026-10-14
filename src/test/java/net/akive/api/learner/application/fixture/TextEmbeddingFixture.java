package net.akive.api.learner.application.fixture;

import java.util.List;

import static java.util.Collections.nCopies;
import static net.akive.api.core.config.llm.fixture.OpenAiPropertiesFixture.EMBEDDING_DIMENSIONS;

public final class TextEmbeddingFixture {

    public static final List<Float> EMBEDDING_VALUES = nCopies(EMBEDDING_DIMENSIONS, 0.1F);

    private TextEmbeddingFixture() {
    }
}
