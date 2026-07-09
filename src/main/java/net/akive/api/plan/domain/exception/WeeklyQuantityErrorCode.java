package net.akive.api.plan.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WeeklyQuantityErrorCode implements ErrorCode {

    PERIOD_OUT_OF_SESSION_PERIOD(HttpStatus.BAD_REQUEST, "WEEKLY_QUANTITY_400_1", "Weekly quantity period must be within session period."),
    AMOUNT_EXCEEDS_TOTAL_QUANTITY_AMOUNT(HttpStatus.BAD_REQUEST, "WEEKLY_QUANTITY_400_2", "Weekly quantity amount exceeds total quantity amount.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
