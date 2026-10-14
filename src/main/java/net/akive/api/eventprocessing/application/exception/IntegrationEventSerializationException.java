package net.akive.api.eventprocessing.application.exception;

public class IntegrationEventSerializationException extends RuntimeException {

    public IntegrationEventSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
