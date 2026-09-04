package net.akive.api.common.exception.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ApplicationError;
import net.akive.api.common.exception.ApplicationErrorType;

import static net.akive.api.common.exception.ApplicationErrorType.UNEXPECTED_SERVER_ERROR;

@Getter
@RequiredArgsConstructor
public enum UnexpectedServerError implements ApplicationError {

    INTERNAL_SERVER_ERROR("Unexpected internal server error.");

    private final String message;

    @Override
    public ApplicationErrorType getType() {
        return UNEXPECTED_SERVER_ERROR;
    }
}
