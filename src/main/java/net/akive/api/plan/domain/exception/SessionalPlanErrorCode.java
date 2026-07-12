package net.akive.api.plan.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SessionalPlanErrorCode implements ErrorCode {

    INVALID_SESSION_PERIOD(HttpStatus.BAD_REQUEST, "SESSIONAL_PLAN_400_1", "Invalid session period."),
    SESSION_PERIOD_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SESSIONAL_PLAN_400_2", "Session period change is not allowed."),
    SESSION_PERIOD_TOO_LONG(HttpStatus.BAD_REQUEST, "SESSIONAL_PLAN_400_3", "Session period is too long."),
    IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "SESSIONAL_PLAN_400_4", "In-progress sessional plan limit exceeded."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "SESSIONAL_PLAN_404_1", "Sessional plan not found."),
    SESSION_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "SESSIONAL_PLAN_409_1", "Session name already exists.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
