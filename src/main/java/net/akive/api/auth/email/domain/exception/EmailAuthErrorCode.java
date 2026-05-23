package net.akive.api.auth.email.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EmailAuthErrorCode implements ErrorCode {

    RESEND_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "EMAIL_AUTH_429_1", "Resend limit exceeded."),
    RESEND_TOO_SOON(HttpStatus.TOO_MANY_REQUESTS, "EMAIL_AUTH_429_2", "Resend request too soon."),
    EMAIL_AUTH_NOT_FOUND(HttpStatus.NOT_FOUND, "EMAIL_AUTH_404_1", "Authentication code not found."),
    EMAIL_AUTH_EXPIRED(HttpStatus.UNAUTHORIZED, "EMAIL_AUTH_401_1", "Authentication code expired."),
    EMAIL_AUTH_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "EMAIL_AUTH_400_1", "Authentication code does not match."),
    EMAIL_AUTH_CODE_ALREADY_VERIFIED(HttpStatus.CONFLICT, "EMAIL_AUTH_409_1", "Authentication code already verified.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
