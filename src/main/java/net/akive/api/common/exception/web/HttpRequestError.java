package net.akive.api.common.exception.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ApplicationError;
import net.akive.api.common.exception.ApplicationErrorType;

@Getter
@RequiredArgsConstructor
public enum HttpRequestError implements ApplicationError {

    METHOD_NOT_ALLOWED(ApplicationErrorType.METHOD_NOT_ALLOWED, "Method not allowed."),
    UNSUPPORTED_MEDIA_TYPE(ApplicationErrorType.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type."),
    ;

    private final ApplicationErrorType type;

    private final String message;
}
