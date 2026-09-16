package net.akive.api.report.adapter.webapi.dto.request;

import jakarta.validation.constraints.NotNull;
import net.akive.api.report.domain.FeedbackType;
import net.akive.api.report.domain.ReportFeedback;

import java.util.ArrayList;
import java.util.List;

import static net.akive.api.report.domain.FeedbackSource.USER;
import static net.akive.api.report.domain.FeedbackType.*;

public record ReportFeedbackRequest(
        @NotNull List<String> highlights,
        @NotNull List<String> improvements,
        @NotNull List<String> notes
) {

    public List<ReportFeedback> to() {
        List<ReportFeedback> feedbackItems = new ArrayList<>();

        addFeedbackItems(feedbackItems, HIGHLIGHT, this.highlights);
        addFeedbackItems(feedbackItems, IMPROVEMENT, this.improvements);
        addFeedbackItems(feedbackItems, NOTE, this.notes);

        return List.copyOf(feedbackItems);
    }

    private static void addFeedbackItems(
            List<ReportFeedback> feedbackItems,
            FeedbackType feedbackType,
            List<String> contents
    ) {
        if (contents == null) {
            return;
        }

        int sortOrder = 1;
        for (String content : contents) {
            if (content == null || content.isBlank()) {
                continue;
            }

            feedbackItems.add(new ReportFeedback(
                    feedbackType,
                    USER,
                    sortOrder++,
                    content.trim()
            ));
        }
    }
}
