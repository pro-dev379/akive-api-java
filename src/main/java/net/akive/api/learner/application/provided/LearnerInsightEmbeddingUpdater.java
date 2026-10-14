package net.akive.api.learner.application.provided;

import net.akive.api.learner.application.event.LearnerInsightChangedEvent;

public interface LearnerInsightEmbeddingUpdater {

    void update(LearnerInsightChangedEvent event);
}
