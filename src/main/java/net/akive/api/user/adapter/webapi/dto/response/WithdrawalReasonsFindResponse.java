package net.akive.api.user.adapter.webapi.dto.response;

import net.akive.api.user.domain.WithdrawalReason;

import java.util.List;

public record WithdrawalReasonsFindResponse(List<WithdrawalReasonResponse> withdrawalReasons) {

    public static WithdrawalReasonsFindResponse from(List<WithdrawalReason> withdrawalReasons) {
        List<WithdrawalReasonResponse> responses = withdrawalReasons.stream()
                .map(WithdrawalReasonResponse::from)
                .toList();

        return new WithdrawalReasonsFindResponse(responses);
    }

    public record WithdrawalReasonResponse(String code, String label) {

        private static WithdrawalReasonResponse from(WithdrawalReason reason) {
            return new WithdrawalReasonResponse(reason.name(), reason.getLabel());
        }
    }
}
