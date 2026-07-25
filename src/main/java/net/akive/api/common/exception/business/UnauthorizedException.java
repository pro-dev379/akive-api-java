package net.akive.api.common.exception.business;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(UnauthorizedError errorCode) {
        super(errorCode);
    }
}