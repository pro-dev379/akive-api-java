package net.akive.api.image.application.fixture;

import net.akive.api.image.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.image.domain.ImageType;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static net.akive.api.image.domain.ImagePolicy.ATTENDANCE_PROOF_MAX_SIZE_BYTES;
import static net.akive.api.image.domain.ImagePolicy.PROFILE_IMAGE_MAX_SIZE_BYTES;
import static net.akive.api.image.domain.ImageType.ATTENDANCE_PROOF;
import static net.akive.api.image.domain.ImageType.PROFILE_IMAGE;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class ImageCommandFixture {

    public static final long IMAGE_FILE_SIZE = 1024L;

    private ImageCommandFixture() {
    }

    public static UploadUrlCreateCommand createUploadUrlCreateCommand() {
        return createUploadUrlCreateCommand(PROFILE_IMAGE, "image/jpeg", IMAGE_FILE_SIZE);
    }

    public static UploadUrlCreateCommand createUploadUrlCreateCommand(
            ImageType imageType,
            String contentType,
            long fileSize
    ) {
        return new UploadUrlCreateCommand(USER_ID, imageType, contentType, fileSize);
    }

    public static Stream<Arguments> supportedImageUploadArguments() {
        return Stream.of(
                Arguments.of(PROFILE_IMAGE, "image/jpeg", "jpg", PROFILE_IMAGE_MAX_SIZE_BYTES),
                Arguments.of(PROFILE_IMAGE, "image/png", "png", IMAGE_FILE_SIZE),
                Arguments.of(PROFILE_IMAGE, "image/webp", "webp", IMAGE_FILE_SIZE),
                Arguments.of(
                        ATTENDANCE_PROOF,
                        "image/jpeg",
                        "jpg",
                        ATTENDANCE_PROOF_MAX_SIZE_BYTES
                ),
                Arguments.of(ATTENDANCE_PROOF, "image/png", "png", IMAGE_FILE_SIZE),
                Arguments.of(ATTENDANCE_PROOF, "image/webp", "webp", IMAGE_FILE_SIZE)
        );
    }

    public static Stream<Arguments> oversizedImageUploadArguments() {
        return Stream.of(
                Arguments.of(PROFILE_IMAGE, PROFILE_IMAGE_MAX_SIZE_BYTES + 1),
                Arguments.of(ATTENDANCE_PROOF, ATTENDANCE_PROOF_MAX_SIZE_BYTES + 1)
        );
    }
}
