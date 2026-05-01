package net.akive.api.user.excpetion;

import net.akive.api.common.exception.BusinessException;

public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException() {
        super(UserErrorCode.EMAIL_ALREADY_EXISTS);
    }
}
