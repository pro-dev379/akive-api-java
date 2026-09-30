package net.akive.api.file.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.FILE_SIZE_EXCEEDED;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FilePolicyTest {

    @ParameterizedTest
    @CsvSource({
            "PROFILE_IMAGE, 5242880",
            "ATTENDANCE_PROOF, 10485760"
    })
    void 이미지_타입별_최대_파일_크기까지_허용한다(ImageType imageType, long maxFileSize) {
        assertThatCode(() -> FilePolicy.validateMaxFileSize(imageType, maxFileSize))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "PROFILE_IMAGE, 5242880",
            "ATTENDANCE_PROOF, 10485760"
    })
    void 이미지_타입별_최대_파일_크기를_초과하면_예외를_던진다(ImageType imageType, long maxFileSize) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> FilePolicy.validateMaxFileSize(imageType, maxFileSize + 1))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(FILE_SIZE_EXCEEDED);
    }
}
