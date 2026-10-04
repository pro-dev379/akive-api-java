package net.akive.api.attendance.adapter.webapi.response;

import net.akive.api.attendance.domain.AttendanceProof;

import java.time.LocalDate;

public record AttendanceProofSubmitResponse(LocalDate proofOn, boolean late) {

    public static AttendanceProofSubmitResponse from(AttendanceProof attendanceProof) {
        return new AttendanceProofSubmitResponse(
                attendanceProof.getProofOn(),
                attendanceProof.getLate()
        );
    }
}
