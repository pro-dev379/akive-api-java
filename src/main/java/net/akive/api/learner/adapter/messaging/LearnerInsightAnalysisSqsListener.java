package net.akive.api.learner.adapter.messaging;

import io.awspring.cloud.sqs.annotation.SqsListener;
import net.akive.api.core.config.aws.ConditionalOnSqsEnabled;
import net.akive.api.eventprocessing.application.event.IntegrationEventMessage;
import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.learner.application.exception.LearnerInsightMessageException;
import net.akive.api.learner.application.provided.LearnerInsightAnalyzer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@ConditionalOnSqsEnabled
class LearnerInsightAnalysisSqsListener {

    private final LearnerInsightAnalyzer learnerInsightAnalyzer;
    private final Map<EventType, LearnerInsightAnalysisCommandMapper> commandMapperByEventType;

    LearnerInsightAnalysisSqsListener(
            List<LearnerInsightAnalysisCommandMapper> commandMappers,
            LearnerInsightAnalyzer learnerInsightAnalyzer
    ) {
        this.commandMapperByEventType = commandMappers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        LearnerInsightAnalysisCommandMapper::supportedEventType,
                        Function.identity()
                ));
        this.learnerInsightAnalyzer = learnerInsightAnalyzer;
    }

    @SqsListener("${event.sqs.learner-insight-analysis.queue-name}")
    void consume(IntegrationEventMessage message) {
        LearnerInsightAnalysisCommandMapper commandMapper = findCommandMapperOrThrow(message);
        learnerInsightAnalyzer.analyze(commandMapper.map(message));
    }

    private LearnerInsightAnalysisCommandMapper findCommandMapperOrThrow(IntegrationEventMessage message) {
        LearnerInsightAnalysisCommandMapper commandMapper = commandMapperByEventType.get(message.eventType());

        if (commandMapper == null) {
            throw new LearnerInsightMessageException(
                    "Learner insight analysis command mapper is not configured: " + message.eventType()
            );
        }

        return commandMapper;
    }
}
