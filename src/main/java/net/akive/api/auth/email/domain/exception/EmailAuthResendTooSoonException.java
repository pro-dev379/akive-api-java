package net.akive.api.auth.email.domain.exception;

import net.akive.api.common.exception.BusinessException;

public class EmailAuthResendTooSoonException extends BusinessException {

    public EmailAuthResendTooSoonException() {
        super(EmailAuthErrorCode.RESEND_TOO_SOON);
    }
}
