package net.akive.api.user.adapter.webapi.dto.request;

import jakarta.validation.constraints.Size;

import static net.akive.api.user.domain.UserPolicy.WITHDRAWAL_REASON_MAX_LENGTH;

public record UserWithdrawRequest(@Size(max = WITHDRAWAL_REASON_MAX_LENGTH) String withdrawReason) {
}
