package net.akive.api.auth.domain.exception;

import net.akive.api.common.exception.BusinessException;

public class EmailVerificationException extends BusinessException {

    public EmailVerificationException(EmailVerificationErrorCode errorCode) {
        super(errorCode);
    }
}
