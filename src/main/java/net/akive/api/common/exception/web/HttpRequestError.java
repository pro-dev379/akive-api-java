package net.akive.api.common.exception.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.Error;
import net.akive.api.common.exception.ErrorDomain;
import org.springframework.http.HttpStatus;

import static net.akive.api.common.exception.ErrorDomain.COMMON;

@Getter
@RequiredArgsConstructor
public enum HttpRequestError implements Error {

    METHOD_NOT_ALLOWED(COMMON, HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed."),
    UNSUPPORTED_MEDIA_TYPE(COMMON, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type."),
    ;

    private final ErrorDomain domain;
    private final HttpStatus status;
    private final String message;
}
