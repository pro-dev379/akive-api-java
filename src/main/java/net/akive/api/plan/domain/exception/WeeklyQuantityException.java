package net.akive.api.plan.domain.exception;

import net.akive.api.common.exception.BusinessException;

public class WeeklyQuantityException extends BusinessException {

    public WeeklyQuantityException(WeeklyQuantityErrorCode errorCode) {
        super(errorCode);
    }
}
