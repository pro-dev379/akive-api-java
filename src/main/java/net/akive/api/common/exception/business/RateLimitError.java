package net.akive.api.common.exception.business;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ApplicationError;
import net.akive.api.common.exception.ApplicationErrorType;

import static net.akive.api.common.exception.ApplicationErrorType.RATE_LIMIT_EXCEEDED;

@Getter
@RequiredArgsConstructor
public enum RateLimitError implements ApplicationError {

    EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT("Email verification renewal is too frequent."),
    EMAIL_VERIFICATION_RENEWAL_LIMIT_EXCEEDED("Email verification renewal limit exceeded."),

    LLM_DAILY_TOKEN_QUOTA_EXCEEDED("Daily LLM token quota exceeded."),
    ;

    private final String message;

    @Override
    public ApplicationErrorType getType() {
        return RATE_LIMIT_EXCEEDED;
    }
}
