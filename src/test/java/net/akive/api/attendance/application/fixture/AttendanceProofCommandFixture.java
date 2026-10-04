package net.akive.api.attendance.application.fixture;

import net.akive.api.attendance.application.provided.dto.AttendanceProofSubmitCommand;

import java.util.UUID;

import static net.akive.api.image.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class AttendanceProofCommandFixture {

    private AttendanceProofCommandFixture() {
    }

    public static AttendanceProofSubmitCommand createAttendanceProofSubmitCommand() {
        return createAttendanceProofSubmitCommand(USER_ID, IMAGE_ASSET_ID);
    }

    public static AttendanceProofSubmitCommand createAttendanceProofSubmitCommand(Long userId) {
        return createAttendanceProofSubmitCommand(userId, IMAGE_ASSET_ID);
    }

    public static AttendanceProofSubmitCommand createAttendanceProofSubmitCommand(UUID imageAssetId) {
        return createAttendanceProofSubmitCommand(USER_ID, imageAssetId);
    }

    public static AttendanceProofSubmitCommand createAttendanceProofSubmitCommand(Long userId, UUID imageAssetId) {
        return new AttendanceProofSubmitCommand(userId, imageAssetId);
    }
}
