package net.akive.api.learner.application.exception;

public class LearnerInsightEmbeddingException extends RuntimeException {

    public LearnerInsightEmbeddingException(String message) {
        super(message);
    }

    public LearnerInsightEmbeddingException(String message, Throwable cause) {
        super(message, cause);
    }
}
