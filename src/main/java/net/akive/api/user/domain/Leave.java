package net.akive.api.user.domain;

import net.akive.api.common.exception.business.BadRequestException;

import static net.akive.api.common.domain.DomainValidator.requireMaxLength;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.common.exception.business.BadRequestError.INVALID_INPUT_VALUE;
import static net.akive.api.user.domain.UserPolicy.LEAVE_REASON_MAX_LENGTH;

public record Leave(boolean isLeave, String reason) {

    public Leave {
        if (isLeave) {
            reason = requireNotBlank(reason);
            requireMaxLength(reason, LEAVE_REASON_MAX_LENGTH);
        } else if (reason != null) {
            throw new BadRequestException(INVALID_INPUT_VALUE);
        }
    }

    public static Leave none() {
        return new Leave(false, null);
    }

    public static Leave completed(String reason) {
        return new Leave(true, reason);
    }
}
