package net.akive.api.auth.email.domain.exception;

import net.akive.api.common.exception.BusinessException;

public class EmailAuthExpiredException extends BusinessException {

    public EmailAuthExpiredException() {
        super(EmailVerificationErrorCode.EXPIRED);
    }
}
