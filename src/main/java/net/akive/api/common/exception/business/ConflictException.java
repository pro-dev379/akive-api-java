package net.akive.api.common.exception.business;

public class ConflictException extends BusinessException {

    public ConflictException(ConflictError errorCode) {
        super(errorCode);
    }
}