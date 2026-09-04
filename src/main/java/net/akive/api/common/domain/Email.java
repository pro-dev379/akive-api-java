package net.akive.api.common.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.util.regex.Pattern;

import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_FORMAT;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public Email {
        value = requireNotBlank(value);

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new BusinessRuleViolationException(INVALID_FORMAT);
        }
    }

    public String value() {
        return value;
    }
}
