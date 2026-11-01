package net.akive.api.learner.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.eventprocessing.application.provided.EventProcessingTracker;
import net.akive.api.learner.application.event.LearnerInsightChangedEvent;
import net.akive.api.learner.application.provided.LearnerInsightEmbeddingUpdater;
import net.akive.api.learner.application.required.LearnerInsightVectorStore;
import net.akive.api.learner.application.required.TextEmbeddingModel;
import net.akive.api.learner.application.required.data.LearnerInsightVectorData;
import net.akive.api.legal.application.provided.LegalRequirementChecker;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.akive.api.eventprocessing.domain.EventConsumer.INSIGHT_EMBEDDING;
import static net.akive.api.learner.application.required.data.TextEmbeddingRequestData.forLearnerInsightStorage;
import static net.akive.api.legal.domain.LegalRequirementScope.AI_FEATURE;

@Service
@RequiredArgsConstructor
class LearnerInsightEmbeddingService implements LearnerInsightEmbeddingUpdater {

    private final EventProcessingTracker eventProcessingTracker;
    private final LegalRequirementChecker legalRequirementChecker;

    private final TextEmbeddingModel textEmbeddingModel;
    private final LearnerInsightVectorStore learnerInsightVectorStore;

    @Override
    public void update(LearnerInsightChangedEvent event) {
        if (eventProcessingTracker.isProcessed(event.eventId(), INSIGHT_EMBEDDING)) {
            return;
        }

        if (!legalRequirementChecker.check(event.userId(), AI_FEATURE).satisfied()) {
            eventProcessingTracker.markProcessed(event.eventId(), event.eventType(), INSIGHT_EMBEDDING);
            return;
        }

        List<Float> embeddingValues = textEmbeddingModel.embed(forLearnerInsightStorage(event.content()));

        learnerInsightVectorStore.save(
                new LearnerInsightVectorData(
                        event.learnerInsightId(),
                        event.userId(),
                        event.status()
                ),
                embeddingValues
        );

        eventProcessingTracker.markProcessed(
                event.eventId(),
                event.eventType(),
                INSIGHT_EMBEDDING
        );
    }
}
