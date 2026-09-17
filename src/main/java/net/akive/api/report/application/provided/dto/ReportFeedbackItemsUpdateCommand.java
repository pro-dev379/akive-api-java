package net.akive.api.report.application.provided.dto;

import net.akive.api.report.domain.ReportFeedback;

import java.util.List;

import static net.akive.api.common.domain.DomainValidator.requireNotEmpty;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record ReportFeedbackItemsUpdateCommand(
        Long reportId,
        Long userId,
        List<ReportFeedback> targetFeedbackItems
) {

    public ReportFeedbackItemsUpdateCommand {
        requirePositive(reportId);
        requirePositive(userId);
        requireNotEmpty(targetFeedbackItems);
    }
}
