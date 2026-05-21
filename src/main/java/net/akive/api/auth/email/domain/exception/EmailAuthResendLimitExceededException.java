package net.akive.api.auth.email.domain.exception;

import net.akive.api.common.exception.BusinessException;

public class EmailAuthResendLimitExceededException extends BusinessException {

    public EmailAuthResendLimitExceededException() {
        super(EmailAuthErrorCode.RESEND_LIMIT_EXCEEDED);
    }
}
