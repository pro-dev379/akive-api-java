package net.akive.api.attendance.application.provided.dto;

import java.util.UUID;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record AttendanceProofSubmitCommand(Long userId, UUID proofImageAssetId) {

    public AttendanceProofSubmitCommand {
        requirePositive(userId);
        requireNonNull(proofImageAssetId);
    }
}
