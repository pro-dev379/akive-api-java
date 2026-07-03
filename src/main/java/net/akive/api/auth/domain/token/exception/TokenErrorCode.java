package net.akive.api.auth.domain.token.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TokenErrorCode implements ErrorCode {

    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_401_1", "Invalid access token."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_401_2", "Invalid refresh token."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_401_3", "Access token expired."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_401_4", "Refresh token expired."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
