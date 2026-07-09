package net.akive.api.plan.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DailyQuantityErrorCode implements ErrorCode {

    DATE_OUT_OF_WEEK_PERIOD(HttpStatus.BAD_REQUEST, "DAILY_QUANTITY_400_1", "Daily quantity date must be within week period."),
    AMOUNT_EXCEEDS_WEEKLY_QUANTITY_AMOUNT(HttpStatus.BAD_REQUEST, "DAILY_QUANTITY_400_2", "Daily quantity amount exceeds weekly quantity amount.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
