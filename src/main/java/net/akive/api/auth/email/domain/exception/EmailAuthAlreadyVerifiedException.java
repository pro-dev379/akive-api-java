package net.akive.api.auth.email.domain.exception;

import net.akive.api.common.exception.BusinessException;

public class EmailAuthAlreadyVerifiedException extends BusinessException {

    public EmailAuthAlreadyVerifiedException() {
        super(EmailAuthErrorCode.EMAIL_AUTH_CODE_ALREADY_VERIFIED);
    }
}
