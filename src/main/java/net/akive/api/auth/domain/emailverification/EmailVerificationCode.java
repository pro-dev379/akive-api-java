package net.akive.api.auth.domain.emailverification;

import net.akive.api.common.exception.business.BusinessRuleViolationException;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_PATTERN;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.EMAIL_VERIFICATION_INVALID_CODE_FORMAT;

public record EmailVerificationCode(String value) {

    public EmailVerificationCode {
        value = validateValue(value);
    }

    public String value() {
        return value;
    }

    private static String validateValue(String value) {
        String validatedValue = requireNotBlank(value);

        validateFormat(validatedValue);

        return validatedValue;
    }

    private static void validateFormat(String value) {
        if (!VERIFICATION_CODE_PATTERN.matcher(value).matches()) {
            throw new BusinessRuleViolationException(EMAIL_VERIFICATION_INVALID_CODE_FORMAT);
        }
    }
}
