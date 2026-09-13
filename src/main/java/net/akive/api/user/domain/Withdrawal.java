package net.akive.api.user.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.LocalDateTime;

import static net.akive.api.common.domain.DomainValidator.requireMaxLength;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_INPUT_VALUE;
import static net.akive.api.user.domain.UserPolicy.WITHDRAWAL_REASON_MAX_LENGTH;

public record Withdrawal(LocalDateTime withdrawnAt, String withdrawalReason) {

    public Withdrawal {
        withdrawalReason = validateWithdrawal(withdrawnAt, withdrawalReason);
    }

    public static Withdrawal none() {
        return new Withdrawal(null, null);
    }

    public static Withdrawal completedWith(LocalDateTime withdrawnAt, String reason) {
        return new Withdrawal(withdrawnAt, reason);
    }

    public boolean isWithdrawn() {
        return this.withdrawnAt != null;
    }

    private static String validateWithdrawal(LocalDateTime withdrawnAt, String withdrawalReason) {
        if (withdrawnAt != null) {
            return requireMaxLength(requireNotBlank(withdrawalReason), WITHDRAWAL_REASON_MAX_LENGTH);
        }

        if (withdrawalReason != null) {
            throw new BusinessRuleViolationException(INVALID_INPUT_VALUE);
        }

        return null;
    }
}
