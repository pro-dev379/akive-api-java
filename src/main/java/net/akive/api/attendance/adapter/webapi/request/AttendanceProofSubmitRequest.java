package net.akive.api.attendance.adapter.webapi.request;

import jakarta.validation.constraints.NotNull;
import net.akive.api.attendance.application.provided.dto.AttendanceProofSubmitCommand;

import java.util.UUID;

public record AttendanceProofSubmitRequest(@NotNull UUID proofImageAssetId) {

    public AttendanceProofSubmitCommand to(Long userId) {
        return new AttendanceProofSubmitCommand(userId, proofImageAssetId);
    }
}
