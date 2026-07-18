package net.akive.api.plan.exception;

import net.akive.api.common.exception.BusinessException;

public class PlanException extends BusinessException {

    public PlanException(PlanErrorCode errorCode) {
        super(errorCode);
    }
}
