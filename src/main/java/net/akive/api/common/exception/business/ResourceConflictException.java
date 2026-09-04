package net.akive.api.common.exception.business;

public class ResourceConflictException extends BusinessException {

    public ResourceConflictException(ResourceConflictError errorCode) {
        super(errorCode);
    }
}
