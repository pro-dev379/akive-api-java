package net.akive.api.auth.email.domain.exception;

import net.akive.api.common.exception.BusinessException;

public class EmailAuthCodeMismatchException extends BusinessException {

    public EmailAuthCodeMismatchException() {
        super(EmailVerificationErrorCode.CODE_MISMATCH);
    }
}
