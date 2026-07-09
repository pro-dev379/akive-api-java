package net.akive.api.auth.domain.emailverification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import net.akive.api.auth.domain.emailverification.exception.EmailVerificationException;

import java.util.regex.Pattern;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING;
import static net.akive.api.auth.domain.emailverification.exception.EmailVerificationErrorCode.INVALID_CODE_FORMAT;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record EmailVerificationCode(String value) {

    private static final Pattern VERIFICATION_CODE_PATTERN = Pattern.compile("\\d{6}");

    @JsonCreator(mode = DELEGATING)
    public EmailVerificationCode {
        value = requireNotBlank(value);
        if (!VERIFICATION_CODE_PATTERN.matcher(value).matches()) {
            throw new EmailVerificationException(INVALID_CODE_FORMAT);
        }
    }

    @JsonValue
    @Override
    public String value() {
        return value;
    }

    public static EmailVerificationCode of(String value) {
        return new EmailVerificationCode(value);
    }
}
