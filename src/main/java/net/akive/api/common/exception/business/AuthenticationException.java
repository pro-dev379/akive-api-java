package net.akive.api.common.exception.business;

public class AuthenticationException extends BusinessException {

    public AuthenticationException(AuthenticationError errorCode) {
        super(errorCode);
    }
}
