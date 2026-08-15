package net.akive.api.auth.domain.emailverification;

import net.akive.api.common.exception.business.BadRequestException;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_PATTERN;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.common.exception.business.BadRequestError.EMAIL_VERIFICATION_INVALID_CODE_FORMAT;

public record EmailVerificationCode(String value) {

    public EmailVerificationCode {
        value = requireNotBlank(value);

        if (!VERIFICATION_CODE_PATTERN.matcher(value).matches()) {
            throw new BadRequestException(EMAIL_VERIFICATION_INVALID_CODE_FORMAT);
        }
    }

    public String value() {
        return value;
    }
}
