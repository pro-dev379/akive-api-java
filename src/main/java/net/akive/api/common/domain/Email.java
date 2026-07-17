package net.akive.api.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.INVALID_EMAIL_FORMAT;
import static net.akive.api.common.domain.DomainValidator.requireMaxLength;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

import net.akive.api.common.domain.exception.DomainValidationException;

public record Email(String value) {

    private static final int MAX_LENGTH = 200;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @JsonCreator(mode = DELEGATING)
    public Email {
        value = requireMaxLength(requireNotBlank(value), MAX_LENGTH);
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new DomainValidationException(INVALID_EMAIL_FORMAT);
        }
    }

    @JsonValue
    @Override
    public String value() {
        return value;
    }
}
