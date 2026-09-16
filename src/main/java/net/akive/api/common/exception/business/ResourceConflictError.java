package net.akive.api.common.exception.business;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ApplicationError;
import net.akive.api.common.exception.ApplicationErrorType;

import static net.akive.api.common.exception.ApplicationErrorType.RESOURCE_CONFLICT;

@Getter
@RequiredArgsConstructor
public enum ResourceConflictError implements ApplicationError {

    EMAIL_ALREADY_EXISTS("Email already exists."),
    NICKNAME_ALREADY_EXISTS("Nickname already exists"),

    SESSION_NAME_ALREADY_EXISTS("Session name already exists."),
    SUBJECT_NAME_ALREADY_EXISTS("Subject name already exists."),

    EMAIL_ALREADY_VERIFIED("Email is already verified."),

    DAILY_LOG_TIME_OVERLAPS("Daily log time overlaps with existing daily log."),

    REPORT_ALREADY_EXISTS("Report already exists.")
    ;

    private final String message;

    @Override
    public ApplicationErrorType getType() {
        return RESOURCE_CONFLICT;
    }
}
