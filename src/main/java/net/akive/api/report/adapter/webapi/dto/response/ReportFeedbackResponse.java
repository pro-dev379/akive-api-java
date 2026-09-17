package net.akive.api.report.adapter.webapi.dto.response;

import net.akive.api.report.domain.ReportFeedback;

import java.util.ArrayList;
import java.util.List;

public record ReportFeedbackResponse(
        List<String> highlights,
        List<String> improvements,
        List<String> notes
) {

    public static ReportFeedbackResponse from(List<ReportFeedback> feedbackItems) {
        List<String> highlights = new ArrayList<>();
        List<String> improvements = new ArrayList<>();
        List<String> notes = new ArrayList<>();

        for (ReportFeedback feedbackItem : feedbackItems) {
            switch (feedbackItem.feedbackType()) {
                case HIGHLIGHT -> highlights.add(feedbackItem.content());
                case IMPROVEMENT -> improvements.add(feedbackItem.content());
                case NOTE -> notes.add(feedbackItem.content());
            }
        }

        return new ReportFeedbackResponse(highlights, improvements, notes);
    }
}
