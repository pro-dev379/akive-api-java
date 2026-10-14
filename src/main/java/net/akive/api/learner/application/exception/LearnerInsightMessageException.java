package net.akive.api.learner.application.exception;

public class LearnerInsightMessageException extends RuntimeException {

    public LearnerInsightMessageException(String message) {
        super(message);
    }

    public LearnerInsightMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
