package net.akive.api.attendance.adapter.webapi.fixture;

import net.akive.api.attendance.adapter.webapi.request.AttendanceProofSubmitRequest;

import static net.akive.api.file.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;

public class AttendanceProofRequestFixture {

    private AttendanceProofRequestFixture() {
    }

    public static AttendanceProofSubmitRequest createAttendanceProofSubmitRequest() {
        return new AttendanceProofSubmitRequest(IMAGE_ASSET_ID);
    }
}
