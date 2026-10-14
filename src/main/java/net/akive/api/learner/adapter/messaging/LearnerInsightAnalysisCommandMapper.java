package net.akive.api.learner.adapter.messaging;

import net.akive.api.eventprocessing.application.event.IntegrationEventMessage;
import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.learner.application.provided.dto.LearnerInsightAnalysisCommand;

interface LearnerInsightAnalysisCommandMapper {

    EventType supportedEventType();

    LearnerInsightAnalysisCommand map(IntegrationEventMessage message);
}
