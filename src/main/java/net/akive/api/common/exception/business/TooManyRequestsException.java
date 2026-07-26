package net.akive.api.common.exception.business;

public class TooManyRequestsException extends BusinessException {

    public TooManyRequestsException(TooManyRequestsError errorCode) {
        super(errorCode);
    }
}
