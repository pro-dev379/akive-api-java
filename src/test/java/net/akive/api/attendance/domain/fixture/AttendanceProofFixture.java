package net.akive.api.attendance.domain.fixture;

import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.file.domain.ImageAsset;
import net.akive.api.user.domain.User;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static net.akive.api.file.domain.ImageType.ATTENDANCE_PROOF;
import static net.akive.api.file.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;

public class AttendanceProofFixture {

    public static final Long ATTENDANCE_PROOF_ID = 1L;
    public static final Long ANOTHER_ATTENDANCE_PROOF_ID = 2L;
    public static final LocalDate PROOF_ON = LocalDate.of(2026, 10, 1);
    public static final LocalTime GOAL_ATTEND_ON = LocalTime.of(9, 0);

    private AttendanceProofFixture() {
    }

    public static ImageAsset createAttendanceProofImageAsset() {
        return ImageAsset.create(IMAGE_ASSET_ID, ATTENDANCE_PROOF);
    }

    public static AttendanceProof submitAttendanceProof(LocalTime goalAttendOn, Instant submittedAt) {
        return submitAttendanceProof(
                createUser(),
                createAttendanceProofImageAsset(),
                goalAttendOn,
                submittedAt
        );
    }

    public static AttendanceProof submitAttendanceProof(
            User user,
            ImageAsset proofImageAsset,
            LocalTime goalAttendOn,
            Instant submittedAt
    ) {
        return AttendanceProof.submit(user, proofImageAsset, goalAttendOn, submittedAt);
    }

    public static AttendanceProof createReconstructedAttendanceProof(
            Long attendanceProofId,
            LocalDate proofOn,
            LocalDate currentDate
    ) {
        return createReconstructedAttendanceProof(
                attendanceProofId,
                createUser(),
                proofOn,
                createAttendanceProofImageAsset(),
                false,
                currentDate
        );
    }

    public static AttendanceProof createReconstructedAttendanceProof(
            Long attendanceProofId,
            User user,
            LocalDate proofOn,
            ImageAsset proofImageAsset,
            boolean late,
            LocalDate currentDate
    ) {
        return AttendanceProof.reconstruct(
                attendanceProofId,
                user,
                proofOn,
                proofImageAsset,
                late,
                currentDate
        );
    }
}
