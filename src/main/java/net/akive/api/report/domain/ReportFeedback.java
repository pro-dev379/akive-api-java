package net.akive.api.report.domain;

import static net.akive.api.common.domain.DomainValidator.*;

public record ReportFeedback(
        FeedbackType feedbackType,
        FeedbackSource feedbackSource,
        long sortOrder,
        String content
) {

    public ReportFeedback {
        requireNonNull(feedbackType);
        requireNonNull(feedbackSource);
        requirePositive(sortOrder);
        requireNotBlank(content);
    }
}
