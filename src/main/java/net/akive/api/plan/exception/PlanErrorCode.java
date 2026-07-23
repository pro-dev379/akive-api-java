package net.akive.api.plan.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlanErrorCode implements ErrorCode {

    INVALID_SESSION_PERIOD(HttpStatus.BAD_REQUEST, "PLAN_400_1", "Invalid session period."),
    SESSION_PERIOD_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PLAN_400_2", "Session period change is not allowed."),
    SESSION_PERIOD_TOO_LONG(HttpStatus.BAD_REQUEST, "PLAN_400_3", "Session period is too long."),
    IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PLAN_400_4", "In-progress sessional plan limit exceeded."),
    SESSIONAL_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAN_404_1", "Sessional plan not found."),
    SESSION_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "PLAN_409_1", "Session name already exists."),

    TOTAL_QUANTITY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PLAN_400_11", "Total quantity limit exceeded."),
    SUBJECT_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "PLAN_409_2", "Subject name already exists."),

    DAILY_QUANTITY_DATE_OUT_OF_WEEK_PERIOD(HttpStatus.BAD_REQUEST, "PLAN_400_6", "Daily quantity date must be within week period."),
    DAILY_QUANTITY_AMOUNT_EXCEEDS_WEEKLY_QUANTITY_AMOUNT(HttpStatus.BAD_REQUEST, "PLAN_400_6", "Daily quantity amount exceeds weekly quantity amount."),
    DAILY_QUANTITY_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PLAN_400_7", "Daily quantity amount mismatch."),

    WEEKLY_QUANTITY_PERIOD_OUT_OF_SESSION_PERIOD(HttpStatus.BAD_REQUEST, "PLAN_400_8", "Weekly quantity period must be within session period."),
    WEEKLY_QUANTITY_AMOUNT_EXCEEDS_TOTAL_QUANTITY_AMOUNT(HttpStatus.BAD_REQUEST, "PLAN_400_9", "Weekly quantity amount exceeds total quantity amount."),

    INVALID_WEEK_PERIOD(HttpStatus.BAD_REQUEST, "PLAN_400_10", "Invalid week period."),
    WEEKLY_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAN_404_2", "Weekly plan not found");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
