package net.akive.api.common.domain;

import net.akive.api.common.exception.business.BadRequestException;

import java.time.LocalDate;
import java.util.Collection;

import static net.akive.api.common.exception.business.BadRequestError.*;

public final class DomainValidator {

    private DomainValidator() {
    }

    public static <T> T requireNonNull(T value) {
        if (value == null) {
            throw new BadRequestException(REQUIRED_VALUE_MISSING);
        }

        return value;
    }

    public static String requireNotBlank(String value) {
        requireNonNull(value);

        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw new BadRequestException(BLANK_VALUE);
        }

        return trimmedValue;
    }

    public static <T extends Collection<?>> T requireNotEmpty(T value) {
        requireNonNull(value);

        if (value.isEmpty()) {
            throw new BadRequestException(EMPTY_COLLECTION);
        }

        return value;
    }

    public static String requireMaxLength(String value, int maxLength) {
        requireNonNull(value);

        if (value.length() > maxLength) {
            throw new BadRequestException(MAX_LENGTH_EXCEEDED);
        }

        return value;
    }

    public static String requireMaxLengthIfPresent(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        if (value.length() > maxLength) {
            throw new BadRequestException(MAX_LENGTH_EXCEEDED);
        }

        return value;
    }

    public static Integer requirePositive(Integer value) {
        requireNonNull(value);

        if (value <= 0) {
            throw new BadRequestException(VALUE_NOT_POSITIVE);
        }

        return value;
    }

    public static Long requirePositive(Long value) {
        requireNonNull(value);

        if (value <= 0) {
            throw new BadRequestException(VALUE_NOT_POSITIVE);
        }

        return value;
    }

    public static Integer requireNonNegative(Integer value) {
        requireNonNull(value);

        if (value < 0) {
            throw new BadRequestException(VALUE_NEGATIVE);
        }

        return value;
    }

    public static Long requireNonNegative(Long value) {
        requireNonNull(value);

        if (value < 0) {
            throw new BadRequestException(VALUE_NEGATIVE);
        }

        return value;
    }

    public static void requireStartNotAfterEnd(LocalDate startOn, LocalDate endOn) {
        requireNonNull(startOn);
        requireNonNull(endOn);

        if (startOn.isAfter(endOn)) {
            throw new BadRequestException(INVALID_DATE_RANGE);
        }
    }
}
