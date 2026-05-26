package net.akive.api.auth.email.domain.exception;

import net.akive.api.common.exception.BusinessException;

public class EmailAuthAlreadyVerifiedException extends BusinessException {

    public EmailAuthAlreadyVerifiedException() {
        super(EmailVerificationErrorCode.ALREADY_VERIFIED);
    }
}
