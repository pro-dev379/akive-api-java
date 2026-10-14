package net.akive.api.learner.application.provided.dto;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record LearnerInsightAnalysisMaterialData(
        AnalysisMaterialRole role,
        String content
) {

    public LearnerInsightAnalysisMaterialData {
        requireNonNull(role);
        content = requireNotBlank(content);
    }
}
