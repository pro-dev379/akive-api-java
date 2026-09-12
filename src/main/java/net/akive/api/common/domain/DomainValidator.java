package net.akive.api.common.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.regex.Pattern;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;

public final class DomainValidator {

    private DomainValidator() {
    }

    public static <T> T requireNonNull(T value) {
        if (value == null) {
            throw new BusinessRuleViolationException(REQUIRED_VALUE_MISSING);
        }

        return value;
    }

    public static String requireNotBlank(String value) {
        requireNonNull(value);

        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw new BusinessRuleViolationException(BLANK_VALUE);
        }

        return trimmedValue;
    }

    public static <T extends Collection<?>> T requireNotEmpty(T value) {
        requireNonNull(value);

        if (value.isEmpty()) {
            throw new BusinessRuleViolationException(EMPTY_COLLECTION);
        }

        return value;
    }

    public static String requireMaxLength(String value, int maxLength) {
        requireNonNull(value);

        if (value.length() > maxLength) {
            throw new BusinessRuleViolationException(MAX_LENGTH_EXCEEDED);
        }

        return value;
    }

    public static String requireMinLength(String value, int minLength) {
        requireNonNull(value);

        if (value.length() < minLength) {
            throw new BusinessRuleViolationException(MIN_LENGTH_NOT_MET);
        }

        return value;
    }

    public static String requireMaxLengthIfPresent(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        return requireMaxLength(value, maxLength);
    }

    public static String requireMatches(String value, Pattern pattern) {
        requireNonNull(value);
        requireNonNull(pattern);

        if (!pattern.matcher(value).matches()) {
            throw new BusinessRuleViolationException(INVALID_FORMAT);
        }

        return value;
    }

    public static Integer requirePositive(Integer value) {
        requireNonNull(value);

        if (value <= 0) {
            throw new BusinessRuleViolationException(VALUE_NOT_POSITIVE);
        }

        return value;
    }

    public static Long requirePositive(Long value) {
        requireNonNull(value);

        if (value <= 0) {
            throw new BusinessRuleViolationException(VALUE_NOT_POSITIVE);
        }

        return value;
    }

    public static Integer requireNonNegative(Integer value) {
        requireNonNull(value);

        if (value < 0) {
            throw new BusinessRuleViolationException(VALUE_NEGATIVE);
        }

        return value;
    }

    public static Long requireNonNegative(Long value) {
        requireNonNull(value);

        if (value < 0) {
            throw new BusinessRuleViolationException(VALUE_NEGATIVE);
        }

        return value;
    }

    public static Long requireNonNegativeIfPresent(Long value) {
        if (value == null) {
            return null;
        }

        return requireNonNegative(value);
    }

    public static void requireStartNotAfterEnd(LocalDate startOn, LocalDate endOn) {
        requireNonNull(startOn);
        requireNonNull(endOn);

        if (startOn.isAfter(endOn)) {
            throw new BusinessRuleViolationException(INVALID_DATE_RANGE);
        }
    }

    public static void requireStartNotAfterEnd(LocalDateTime startOn, LocalDateTime endOn) {
        requireNonNull(startOn);
        requireNonNull(endOn);

        if (startOn.isAfter(endOn)) {
            throw new BusinessRuleViolationException(INVALID_DATE_RANGE);
        }
    }
}
