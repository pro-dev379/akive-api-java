package net.akive.api.auth.domain.emailverification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import net.akive.api.common.exception.business.BadRequestException;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_PATTERN;
import static net.akive.api.common.exception.business.BadRequestError.EMAIL_VERIFICATION_INVALID_CODE_FORMAT;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record EmailVerificationCode(String value) {

    @JsonCreator(mode = DELEGATING)
    public EmailVerificationCode {
        value = requireNotBlank(value);
        if (!VERIFICATION_CODE_PATTERN.matcher(value).matches()) {
            throw new BadRequestException(EMAIL_VERIFICATION_INVALID_CODE_FORMAT);
        }
    }

    @JsonValue
    @Override
    public String value() {
        return value;
    }
}
