package net.akive.api.plan.domain.exception;

import net.akive.api.common.exception.BusinessException;

public class WeekPeriodException extends BusinessException {

    public WeekPeriodException(WeekPeriodErrorCode errorCode) {
        super(errorCode);
    }
}
