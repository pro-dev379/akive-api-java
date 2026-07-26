package net.akive.api.common.exception.business;

public class BadRequestException extends BusinessException {

    public BadRequestException(BadRequestError errorCode) {
        super(errorCode);
    }
}
