package net.akive.api.plan.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SessionalPlanErrorCode implements ErrorCode {

    INVALID_SESSION_PERIOD(HttpStatus.BAD_REQUEST, "SESSIONAL_PLAN_400_3", "Invalid session period."),
    SESSION_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "SESSIONAL_PLAN_409_1", "Session name already exists.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
