package net.akive.api.learner.application.required.data;

import net.akive.api.learner.domain.LearnerInsight;

import java.util.List;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotEmpty;

public record LearnerInsightAnalysisRequestData(
        List<String> evidenceContents,
        List<String> referenceContents,
        List<LearnerInsight> currentInsights
) {

    public LearnerInsightAnalysisRequestData {
        evidenceContents = List.copyOf(requireNotEmpty(evidenceContents));
        referenceContents = List.copyOf(requireNonNull(referenceContents));
        currentInsights = List.copyOf(requireNonNull(currentInsights));
    }
}
