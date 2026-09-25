package net.akive.api.file.domain;

import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.Duration;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.FILE_SIZE_EXCEEDED;

@NoArgsConstructor(access = PRIVATE)
public final class FileUploadPolicy {

    public static final Duration UPLOAD_URL_EXPIRATION_DURATION = Duration.ofMinutes(10);
    public static final long PROFILE_IMAGE_MAX_SIZE_BYTES = 5L * 1024 * 1024;
    public static final long ATTENDANCE_PROOF_MAX_SIZE_BYTES = 10L * 1024 * 1024;

    public static void validateMaxFileSize(UploadPurpose uploadPurpose, long fileSize) {
        long maxSize = switch (uploadPurpose) {
            case PROFILE_IMAGE -> PROFILE_IMAGE_MAX_SIZE_BYTES;
            case ATTENDANCE_PROOF -> ATTENDANCE_PROOF_MAX_SIZE_BYTES;
        };

        if (fileSize > maxSize) {
            throw new BusinessRuleViolationException(FILE_SIZE_EXCEEDED);
        }
    }
}
