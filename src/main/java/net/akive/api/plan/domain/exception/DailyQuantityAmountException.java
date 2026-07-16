package net.akive.api.plan.domain.exception;

import net.akive.api.common.exception.BusinessException;

public class DailyQuantityAmountException extends BusinessException {

    public DailyQuantityAmountException(DailyQuantityAmountErrorCode errorCode) {
        super(errorCode);
    }
}