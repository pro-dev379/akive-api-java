package net.akive.api.common.exception.business;

public class RateLimitExceededException extends BusinessException {

    public RateLimitExceededException(RateLimitError errorCode) {
        super(errorCode);
    }
}
