package net.akive.api.auth.domain.token.exception;

import net.akive.api.common.exception.BusinessException;

public class TokenException extends BusinessException {

    public TokenException(TokenErrorCode errorCode) {
        super(errorCode);
    }
}
