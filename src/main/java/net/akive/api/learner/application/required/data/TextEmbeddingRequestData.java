package net.akive.api.learner.application.required.data;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.learner.application.required.data.TextEmbeddingPurpose.LEARNER_INSIGHT_SEARCH;
import static net.akive.api.learner.application.required.data.TextEmbeddingPurpose.LEARNER_INSIGHT_STORAGE;

public record TextEmbeddingRequestData(
        Long userId,
        String text,
        TextEmbeddingPurpose purpose
) {

    public TextEmbeddingRequestData {
        requirePositiveIfPresent(userId);
        text = requireNotBlank(text);
        requireNonNull(purpose);

        if (purpose == LEARNER_INSIGHT_SEARCH) {
            requirePositive(userId);
        }
    }

    public static TextEmbeddingRequestData forLearnerInsightStorage(String text) {
        return new TextEmbeddingRequestData(null, text, LEARNER_INSIGHT_STORAGE);
    }

    public static TextEmbeddingRequestData forLearnerInsightSearch(Long userId, String text) {
        return new TextEmbeddingRequestData(userId, text, LEARNER_INSIGHT_SEARCH);
    }
}
