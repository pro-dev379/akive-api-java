package net.akive.api.plan.domain.exception;

import net.akive.api.common.exception.BusinessException;

public class SessionalPlanException extends BusinessException {

    public SessionalPlanException(SessionalPlanErrorCode errorCode) {
        super(errorCode);
    }
}
