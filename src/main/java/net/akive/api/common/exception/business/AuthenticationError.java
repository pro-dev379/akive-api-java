package net.akive.api.common.exception.business;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ApplicationError;
import net.akive.api.common.exception.ApplicationErrorType;

import static net.akive.api.common.exception.ApplicationErrorType.AUTHENTICATION_ERROR;

@Getter
@RequiredArgsConstructor
public enum AuthenticationError implements ApplicationError {

    INVALID_EMAIL_OR_PASSWORD("Invalid email or password."),

    INVALID_ACCESS_TOKEN("Invalid access token."),
    INVALID_REFRESH_TOKEN("Invalid refresh token."),

    ACCESS_TOKEN_EXPIRED("Access token expired."),
    REFRESH_TOKEN_EXPIRED("Refresh token expired."),

    EMAIL_VERIFICATION_CODE_EXPIRED("Email verification code expired."),
    ;

    private final String message;

    @Override
    public ApplicationErrorType getType() {
        return AUTHENTICATION_ERROR;
    }
}
