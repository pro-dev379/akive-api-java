package net.akive.api.common.exception;

public interface ApplicationError {

    ApplicationErrorType getType();

    String getMessage();

    default String getCode() {
        if (!(this instanceof Enum<?> enumConstant)) {
            throw new IllegalStateException("ApplicationError must be implemented by enum.");
        }

        return enumConstant.name();
    }
}
