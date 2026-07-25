package net.akive.api.common.exception;

import org.springframework.http.HttpStatus;

public interface Error {

    ErrorDomain getDomain();
    HttpStatus getStatus();
    String getMessage();

    default String getCode() {
        if (!(this instanceof Enum<?> errorCode)) {
            throw new IllegalStateException("ErrorCode must be implemented by enum.");
        }

        return "%s_%d_%s".formatted(
                getDomain().name(),
                getStatus().value(),
                errorCode.name()
        );
    }
}
