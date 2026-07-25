package net.akive.api.common.exception.business;

public class NotFoundException extends BusinessException {

    public NotFoundException(NotFoundError errorCode) {
        super(errorCode);
    }
}