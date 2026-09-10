package net.akive.api.user.application.provided.dto;

import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record UserWithdrawCommand(
        Long userId,
        String withdrawReason
) {

    public UserWithdrawCommand {
        requirePositive(userId);
    }

    public static UserWithdrawCommand of(Long userId, String withdrawReason) {
        return new UserWithdrawCommand(userId, withdrawReason);
    }
}
