package net.akive.api.user.excpetion;

import net.akive.api.common.exception.BusinessException;

public class UserException extends BusinessException {

    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }
}
