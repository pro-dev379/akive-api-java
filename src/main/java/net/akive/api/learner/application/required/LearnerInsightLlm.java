package net.akive.api.learner.application.required;

import net.akive.api.learner.application.required.data.LearnerInsightAnalysisRequestData;
import net.akive.api.learner.application.required.data.LearnerInsightAnalysisResultData;

public interface LearnerInsightLlm {

    LearnerInsightAnalysisResultData analyze(LearnerInsightAnalysisRequestData request);
}
