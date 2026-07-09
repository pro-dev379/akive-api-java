package net.akive.api.common.domain.exception;

import net.akive.api.common.exception.BusinessException;

public class DomainValidationException extends BusinessException {

    public DomainValidationException(DomainValidationErrorCode errorCode) {
        super(errorCode);
    }
}
