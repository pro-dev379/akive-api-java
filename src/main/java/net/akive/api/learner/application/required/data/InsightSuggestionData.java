package net.akive.api.learner.application.required.data;

import net.akive.api.learner.domain.EvidenceStance;
import net.akive.api.learner.domain.InsightType;

public record InsightSuggestionData(
        InsightAction action,
        Long targetInsightId,
        InsightType insightType,
        String content,
        EvidenceStance stance,
        String evidenceContent
) {
}
