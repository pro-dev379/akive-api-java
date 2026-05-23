package net.akive.api.auth.email.domain.exception;

import net.akive.api.common.exception.BusinessException;

public class EmailAuthNotFoundException extends BusinessException {

    public EmailAuthNotFoundException() {
        super(EmailAuthErrorCode.EMAIL_AUTH_NOT_FOUND);
    }
}
