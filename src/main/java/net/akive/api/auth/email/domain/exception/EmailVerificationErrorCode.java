package net.akive.api.auth.email.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EmailVerificationErrorCode implements ErrorCode {

    RESEND_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "EMAIL_VERIFICATION_429_1", "Resend limit exceeded."),
    RESEND_TOO_SOON(HttpStatus.TOO_MANY_REQUESTS, "EMAIL_VERIFICATION_429_2", "Resend request too soon."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "EMAIL_VERIFICATION_404_1", "Verification code not found."),
    EXPIRED(HttpStatus.UNAUTHORIZED, "EMAIL_VERIFICATION_401_1", "Verification code expired."),
    CODE_MISMATCH(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION_400_1", "Verification code does not match."),
    ALREADY_VERIFIED(HttpStatus.CONFLICT, "EMAIL_VERIFICATION_409_1", "Verification code already verified.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
