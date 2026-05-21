package net.akive.api.auth.email.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EmailAuthErrorCode implements ErrorCode {

    RESEND_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "EMAIL_AUTH_429_1", "Resend limit exceeded."),
    RESEND_TOO_SOON(HttpStatus.TOO_MANY_REQUESTS, "EMAIL_AUTH_429_2", "Resend request too soon.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
