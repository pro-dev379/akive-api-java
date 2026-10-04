package net.akive.api.file.application.fixture;

import net.akive.api.file.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.file.domain.ImageType;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static net.akive.api.file.domain.FilePolicy.ATTENDANCE_PROOF_MAX_SIZE_BYTES;
import static net.akive.api.file.domain.FilePolicy.PROFILE_IMAGE_MAX_SIZE_BYTES;
import static net.akive.api.file.domain.ImageType.ATTENDANCE_PROOF;
import static net.akive.api.file.domain.ImageType.PROFILE_IMAGE;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class FileCommandFixture {

    public static final long FILE_SIZE = 1024L;

    private FileCommandFixture() {
    }

    public static UploadUrlCreateCommand createUploadUrlCreateCommand() {
        return createUploadUrlCreateCommand(PROFILE_IMAGE, "image/jpeg", FILE_SIZE);
    }

    public static UploadUrlCreateCommand createUploadUrlCreateCommand(
            ImageType imageType,
            String contentType,
            long fileSize
    ) {
        return new UploadUrlCreateCommand(USER_ID, imageType, contentType, fileSize);
    }

    public static Stream<Arguments> allowedUploadFiles() {
        return Stream.of(
                Arguments.of(PROFILE_IMAGE, "image/jpeg", "jpg", PROFILE_IMAGE_MAX_SIZE_BYTES),
                Arguments.of(PROFILE_IMAGE, "image/png", "png", FILE_SIZE),
                Arguments.of(PROFILE_IMAGE, "image/webp", "webp", FILE_SIZE),
                Arguments.of(
                        ATTENDANCE_PROOF,
                        "image/jpeg",
                        "jpg",
                        ATTENDANCE_PROOF_MAX_SIZE_BYTES
                ),
                Arguments.of(ATTENDANCE_PROOF, "image/png", "png", FILE_SIZE),
                Arguments.of(ATTENDANCE_PROOF, "image/webp", "webp", FILE_SIZE)
        );
    }

    public static Stream<Arguments> oversizedUploadFiles() {
        return Stream.of(
                Arguments.of(PROFILE_IMAGE, PROFILE_IMAGE_MAX_SIZE_BYTES + 1),
                Arguments.of(ATTENDANCE_PROOF, ATTENDANCE_PROOF_MAX_SIZE_BYTES + 1)
        );
    }
}
