package net.akive.api.plan.domain.exception;

import net.akive.api.common.exception.BusinessException;

public class DailyQuantityException extends BusinessException {

    public DailyQuantityException(DailyQuantityErrorCode errorCode) {
        super(errorCode);
    }
}
