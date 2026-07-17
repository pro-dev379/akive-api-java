package net.akive.api.plan.domain.exception;

import net.akive.api.common.exception.BusinessException;
import net.akive.api.common.exception.ErrorCode;

public class PlanException extends BusinessException {

    public PlanException(ErrorCode errorCode) {
        super(errorCode);
    }
}
