package net.akive.api.user.excpetion;

import net.akive.api.common.exception.BusinessException;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super(UserErrorCode.USER_NOT_FOUND);
    }
}
