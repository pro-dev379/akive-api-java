package net.akive.api.attendance.adapter.webapi.response;

import net.akive.api.image.domain.ProcessingStatus;

import java.net.URI;

public record AttendanceProofFindResponse(ProofImageResponse proofImage, boolean late) {

    public static AttendanceProofFindResponse of(
            ProcessingStatus imageStatus,
            boolean late,
            URI proofImageUrl
    ) {
        return new AttendanceProofFindResponse(
                new ProofImageResponse(imageStatus, proofImageUrl),
                late
        );
    }

    public record ProofImageResponse(ProcessingStatus processingStatus, URI url) {}
}
