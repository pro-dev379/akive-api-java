package net.akive.api.auth.domain.login.exception;

import net.akive.api.common.exception.BusinessException;

public class LoginException extends BusinessException {

    public LoginException(LoginErrorCode errorCode) {
        super(errorCode);
    }
}
