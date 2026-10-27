package net.akive.api.user.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.Instant;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_INPUT_VALUE;

public record Withdrawal(Instant withdrawnAt, WithdrawalReason withdrawalReason) {

    public Withdrawal {
        withdrawalReason = validateWithdrawal(withdrawnAt, withdrawalReason);
    }

    public static Withdrawal none() {
        return new Withdrawal(null, null);
    }

    public static Withdrawal completedWith(Instant withdrawnAt, WithdrawalReason reason) {
        return new Withdrawal(withdrawnAt, reason);
    }

    public boolean isWithdrawn() {
        return this.withdrawnAt != null;
    }

    private static WithdrawalReason validateWithdrawal(
            Instant withdrawnAt,
            WithdrawalReason withdrawalReason
    ) {
        if (withdrawnAt != null) {
            return requireNonNull(withdrawalReason);
        }

        if (withdrawalReason != null) {
            throw new BusinessRuleViolationException(INVALID_INPUT_VALUE);
        }

        return null;
    }
}
