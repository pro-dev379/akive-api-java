package net.akive.api.image.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.IMAGE_FILE_SIZE_EXCEEDED;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ImagePolicyTest {

    @ParameterizedTest
    @CsvSource({
            "PROFILE_IMAGE, 5242880",
            "ATTENDANCE_PROOF, 10485760"
    })
    void 이미지_타입별_최대_파일_사이즈까지_허용한다(ImageType imageType, long maxFileSize) {
        assertThatCode(() -> ImagePolicy.validateMaxFileSize(imageType, maxFileSize))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "PROFILE_IMAGE, 5242880",
            "ATTENDANCE_PROOF, 10485760"
    })
    void 이미지_타입별_최대_파일_사이즈를_초과하면_예외를_던진다(ImageType imageType, long maxFileSize) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> ImagePolicy.validateMaxFileSize(imageType, maxFileSize + 1))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(IMAGE_FILE_SIZE_EXCEEDED);
    }
}
