package net.akive.api.common.exception.business;

public class AuthorizationException extends BusinessException {

    public AuthorizationException(AuthorizationError error) {
        super(error);
    }
}
