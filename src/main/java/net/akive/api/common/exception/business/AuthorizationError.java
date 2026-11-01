package net.akive.api.common.exception.business;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ApplicationError;
import net.akive.api.common.exception.ApplicationErrorType;

import static net.akive.api.common.exception.ApplicationErrorType.FORBIDDEN;

@Getter
@RequiredArgsConstructor
public enum AuthorizationError implements ApplicationError {

    CORE_SERVICE_LEGAL_REQUIREMENTS_NOT_SATISFIED("Core service legal requirements are not satisfied."),
    AI_FEATURE_LEGAL_REQUIREMENTS_NOT_SATISFIED("AI feature legal requirements are not satisfied."),
    ;

    private final String message;

    @Override
    public ApplicationErrorType getType() {
        return FORBIDDEN;
    }
}
