package net.akive.api.common.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DomainValidationErrorCode implements ErrorCode {

    REQUIRED_VALUE_MISSING(HttpStatus.BAD_REQUEST, "COMMON_DOMAIN_400_1", "Required value is missing."),
    BLANK_VALUE(HttpStatus.BAD_REQUEST, "COMMON_DOMAIN_400_2", "Value must not be blank."),
    MAX_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "COMMON_DOMAIN_400_3", "Value exceeds max length."),
    VALUE_NOT_POSITIVE(HttpStatus.BAD_REQUEST, "COMMON_DOMAIN_400_4", "Value must be positive."),
    VALUE_NEGATIVE(HttpStatus.BAD_REQUEST, "COMMON_DOMAIN_400_5", "Value must not be negative."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "COMMON_DOMAIN_400_6", "Start date must not be after end date."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "COMMON_DOMAIN_400_7", "Invalid email format.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
