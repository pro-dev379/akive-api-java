package net.akive.api.common.exception.business;

import lombok.Getter;
import net.akive.api.common.exception.ApplicationError;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final ApplicationError error;
    private final String detail;

    protected BusinessException(ApplicationError error) {
        this(error, null);
    }

    protected BusinessException(ApplicationError error, String detail) {
        super(error.getMessage());
        this.error = error;
        this.detail = detail;
    }
}
