package net.akive.api.learner.adapter.messaging;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.aws.ConditionalOnSqsEnabled;
import net.akive.api.eventprocessing.application.event.IntegrationEventMessage;
import net.akive.api.learner.application.provided.LearnerInsightEmbeddingUpdater;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnSqsEnabled
class LearnerInsightEmbeddingSqsListener {

    private final LearnerInsightEmbeddingUpdater learnerInsightEmbeddingUpdater;
    private final LearnerInsightChangedEventMapper learnerInsightChangedEventMapper;

    @SqsListener("${event.sqs.learner-insight-embedding.queue-name}")
    void consume(IntegrationEventMessage message) {
        learnerInsightEmbeddingUpdater.update(learnerInsightChangedEventMapper.map(message));
    }
}
