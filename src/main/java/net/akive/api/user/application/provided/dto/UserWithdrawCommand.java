package net.akive.api.user.application.provided.dto;

import net.akive.api.user.domain.WithdrawalReason;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record UserWithdrawCommand(Long userId, WithdrawalReason withdrawReason) {

    public UserWithdrawCommand {
        requirePositive(userId);
        requireNonNull(withdrawReason);
    }

    public static UserWithdrawCommand of(Long userId, WithdrawalReason withdrawReason) {
        return new UserWithdrawCommand(userId, withdrawReason);
    }
}
