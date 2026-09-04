package net.akive.api.common.exception;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ApplicationErrorHttpStatusMapper {

    public static HttpStatus toHttpStatus(ApplicationError error) {
        return switch (error.getType()) {
            case BUSINESS_RULE_VIOLATION -> HttpStatus.BAD_REQUEST;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case RESOURCE_CONFLICT -> HttpStatus.CONFLICT;
            case AUTHENTICATION_ERROR -> HttpStatus.UNAUTHORIZED;
            case RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            case UNEXPECTED_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case METHOD_NOT_ALLOWED -> HttpStatus.METHOD_NOT_ALLOWED;
            case UNSUPPORTED_MEDIA_TYPE -> HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        };
    }
}
