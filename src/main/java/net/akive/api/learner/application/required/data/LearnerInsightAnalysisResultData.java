package net.akive.api.learner.application.required.data;

import java.util.List;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record LearnerInsightAnalysisResultData(List<InsightSuggestionData> suggestions) {

    public LearnerInsightAnalysisResultData {
        suggestions = List.copyOf(requireNonNull(suggestions));
    }
}
