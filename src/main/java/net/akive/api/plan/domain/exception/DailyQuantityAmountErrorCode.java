package net.akive.api.plan.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DailyQuantityAmountErrorCode implements ErrorCode {

    DAILY_QUANTITY_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "DAILY_QUANTITY_400_1", "Daily quantity amount mismatch.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}