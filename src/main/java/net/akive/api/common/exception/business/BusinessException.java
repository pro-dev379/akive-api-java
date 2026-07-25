package net.akive.api.common.exception.business;

import lombok.Getter;
import net.akive.api.common.exception.Error;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final net.akive.api.common.exception.Error error;
    private final String detail;

    protected BusinessException(net.akive.api.common.exception.Error error) {
        this(error, null);
    }

    protected BusinessException(Error error, String detail) {
        super(error.getMessage());
        this.error = error;
        this.detail = detail;
    }
}
