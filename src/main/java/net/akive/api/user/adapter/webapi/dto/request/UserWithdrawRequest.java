package net.akive.api.user.adapter.webapi.dto.request;

import jakarta.validation.constraints.NotNull;
import net.akive.api.user.domain.WithdrawalReason;

public record UserWithdrawRequest(@NotNull WithdrawalReason withdrawReason) {
}
