package net.akive.api.common.exception.business;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.Error;
import net.akive.api.common.exception.ErrorDomain;
import org.springframework.http.HttpStatus;

import static net.akive.api.common.exception.ErrorDomain.*;

@Getter
@RequiredArgsConstructor
public enum UnauthorizedError implements Error {

    INVALID_EMAIL_OR_PASSWORD(LOGIN, "Invalid email or password."),

    INVALID_ACCESS_TOKEN(TOKEN, "Invalid access token."),
    INVALID_REFRESH_TOKEN(TOKEN, "Invalid refresh token."),
    ACCESS_TOKEN_EXPIRED(TOKEN, "Access token expired."),
    REFRESH_TOKEN_EXPIRED(TOKEN, "Refresh token expired."),

    EMAIL_VERIFICATION_CODE_EXPIRED(EMAIL_VERIFICATION, "Email verification code expired."),
    ;

    private final ErrorDomain domain;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
