package net.akive.api.common.domain;

import net.akive.api.common.domain.exception.DomainValidationException;

import java.time.LocalDate;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.*;

public final class DomainValidator {

    private DomainValidator() {
    }

    public static <T> T requireNonNull(T value) {
        if (value == null) {
            throw new DomainValidationException(REQUIRED_VALUE_MISSING);
        }

        return value;
    }

    public static String requireNotBlank(String value) {
        requireNonNull(value);

        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw new DomainValidationException(BLANK_VALUE);
        }

        return trimmedValue;
    }

    public static String requireMaxLength(String value, int maxLength) {
        requireNonNull(value);

        if (value.length() > maxLength) {
            throw new DomainValidationException(MAX_LENGTH_EXCEEDED);
        }

        return value;
    }

    public static String requireMaxLengthIfPresent(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        if (value.length() > maxLength) {
            throw new DomainValidationException(MAX_LENGTH_EXCEEDED);
        }

        return value;
    }

    public static Integer requirePositive(Integer value) {
        requireNonNull(value);

        if (value <= 0) {
            throw new DomainValidationException(VALUE_NOT_POSITIVE);
        }

        return value;
    }

    public static Long requirePositive(Long value) {
        requireNonNull(value);

        if (value <= 0) {
            throw new DomainValidationException(VALUE_NOT_POSITIVE);
        }

        return value;
    }

    public static Integer requireNonNegative(Integer value) {
        requireNonNull(value);

        if (value < 0) {
            throw new DomainValidationException(VALUE_NEGATIVE);
        }

        return value;
    }

    public static Long requireNonNegative(Long value) {
        requireNonNull(value);

        if (value < 0) {
            throw new DomainValidationException(VALUE_NEGATIVE);
        }

        return value;
    }

    public static void requireStartNotAfterEnd(LocalDate startOn, LocalDate endOn) {
        requireNonNull(startOn);
        requireNonNull(endOn);

        if (startOn.isAfter(endOn)) {
            throw new DomainValidationException(INVALID_DATE_RANGE);
        }
    }
}
