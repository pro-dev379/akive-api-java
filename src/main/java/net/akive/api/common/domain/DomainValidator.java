package net.akive.api.common.domain;

import net.akive.api.common.exception.business.BadRequestException;

import java.time.LocalDate;
import java.util.Collection;
import java.util.regex.Pattern;

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

    public static String requireMinLength(String value, int minLength) {
        requireNonNull(value);

        if (value.length() < minLength) {
            throw new BadRequestException(MIN_LENGTH_NOT_MET);
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

    public static String requireMatches(String value, Pattern pattern) {
        requireNonNull(value);
        requireNonNull(pattern);

        if (!pattern.matcher(value).matches()) {
            throw new BadRequestException(INVALID_FORMAT);
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
