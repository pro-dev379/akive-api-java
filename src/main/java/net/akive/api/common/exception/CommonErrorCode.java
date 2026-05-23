package net.akive.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500_1", "Unexpected server error."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_400_1", "Invalid request."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404_1", "Requested resource not found.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
