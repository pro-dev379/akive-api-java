package net.akive.api.learner.application.provided;

import net.akive.api.learner.application.provided.dto.LearnerInsightAnalysisCommand;

public interface LearnerInsightAnalyzer {

    void analyze(LearnerInsightAnalysisCommand command);
}
