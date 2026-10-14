package net.akive.api.eventprocessing.application.exception;

public class IntegrationEventPublicationException extends RuntimeException {

    public IntegrationEventPublicationException(String message) {
        super(message);
    }

    public IntegrationEventPublicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
