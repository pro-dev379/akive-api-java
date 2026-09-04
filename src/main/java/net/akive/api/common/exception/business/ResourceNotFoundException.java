package net.akive.api.common.exception.business;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(ResourceNotFoundError errorCode) {
        super(errorCode);
    }
}
