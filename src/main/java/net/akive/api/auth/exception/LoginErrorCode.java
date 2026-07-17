package net.akive.api.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LoginErrorCode implements ErrorCode {

    INVALID_EMAIL_OR_PASSWORD(HttpStatus.UNAUTHORIZED, "LOGIN_401_1", "Invalid email or password.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
