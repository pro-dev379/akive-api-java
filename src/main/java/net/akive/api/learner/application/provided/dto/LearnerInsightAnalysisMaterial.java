package net.akive.api.learner.application.provided.dto;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record LearnerInsightAnalysisMaterial(
        AnalysisMaterialRole role,
        String content
) {

    public LearnerInsightAnalysisMaterial {
        requireNonNull(role);
        content = requireNotBlank(content);
    }
}
