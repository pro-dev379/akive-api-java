package net.akive.api.plan.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WeekPeriodErrorCode implements ErrorCode {

    INVALID_WEEK_PERIOD(HttpStatus.BAD_REQUEST, "WEEK_PERIOD_400_1", "Invalid week period.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
