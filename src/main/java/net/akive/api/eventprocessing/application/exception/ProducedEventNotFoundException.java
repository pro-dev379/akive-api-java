package net.akive.api.eventprocessing.application.exception;

public class ProducedEventNotFoundException extends RuntimeException {

    public ProducedEventNotFoundException(Long producedEventId) {
        super("Produced event could not be found: " + producedEventId);
    }
}
