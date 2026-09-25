package net.akive.api.file.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.FILE_SIZE_EXCEEDED;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FileUploadPolicyTest {

    @ParameterizedTest
    @CsvSource({
            "PROFILE_IMAGE, 5242880",
            "ATTENDANCE_PROOF, 10485760"
    })
    void 업로드_목적별_최대_파일_크기까지_허용한다(UploadPurpose uploadPurpose, long maxFileSize) {
        assertThatCode(() -> FileUploadPolicy.validateMaxFileSize(uploadPurpose, maxFileSize))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "PROFILE_IMAGE, 5242880",
            "ATTENDANCE_PROOF, 10485760"
    })
    void 업로드_목적별_최대_파일_크기를_초과하면_예외를_던진다(UploadPurpose uploadPurpose, long maxFileSize) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> FileUploadPolicy.validateMaxFileSize(uploadPurpose, maxFileSize + 1))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(FILE_SIZE_EXCEEDED);
    }
}
