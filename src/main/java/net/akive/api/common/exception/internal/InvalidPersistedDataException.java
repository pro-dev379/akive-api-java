package net.akive.api.common.exception.internal;

public class InvalidPersistedDataException extends RuntimeException {

    public InvalidPersistedDataException(String message) {
        super(message);
    }

    public InvalidPersistedDataException(String message, Throwable cause) {
        super(message, cause);
    }
}