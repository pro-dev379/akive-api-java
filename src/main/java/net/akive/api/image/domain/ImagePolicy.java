package net.akive.api.image.domain;

import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.Duration;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.IMAGE_FILE_SIZE_EXCEEDED;

@NoArgsConstructor(access = PRIVATE)
public final class ImagePolicy {

    public static final int OBJECT_KEY_MAX_LENGTH = 512;
    public static final Duration UPLOAD_URL_EXPIRATION_DURATION = Duration.ofMinutes(10);
    public static final long PROFILE_IMAGE_MAX_SIZE_BYTES = 5L * 1024 * 1024;
    public static final long ATTENDANCE_PROOF_MAX_SIZE_BYTES = 10L * 1024 * 1024;

    public static void validateMaxFileSize(ImageType imageType, long fileSize) {
        long maxSize = switch (imageType) {
            case PROFILE_IMAGE -> PROFILE_IMAGE_MAX_SIZE_BYTES;
            case ATTENDANCE_PROOF -> ATTENDANCE_PROOF_MAX_SIZE_BYTES;
        };

        if (fileSize > maxSize) {
            throw new BusinessRuleViolationException(IMAGE_FILE_SIZE_EXCEEDED);
        }
    }
}
