package net.akive.api.image.adapter.persistence.converter;

import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import org.junit.jupiter.api.Test;

import static net.akive.api.image.domain.ImageFormat.WEBP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ImageFormatConverterTest {

    private final ImageFormatConverter converter = new ImageFormatConverter();

    @Test
    void 이미지_포맷을_데이터베이스_값으로_변환한다() {
        assertThat(converter.convertToDatabaseColumn(WEBP)).isEqualTo("webp");
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void 데이터베이스_값을_이미지_포맷으로_변환한다() {
        assertThat(converter.convertToEntityAttribute("webp")).isEqualTo(WEBP);
    }

    @Test
    void 저장된_이미지_포맷이_null이면_복원할_수_없다() {
        assertThatExceptionOfType(InvalidPersistedDataException.class)
                .isThrownBy(() -> converter.convertToEntityAttribute(null))
                .withMessage("Persisted image format must not be null.");
    }

    @Test
    void 지원하지_않는_이미지_포맷이면_복원할_수_없다() {
        assertThatExceptionOfType(InvalidPersistedDataException.class)
                .isThrownBy(() -> converter.convertToEntityAttribute("jpg"))
                .withMessage("Unsupported persisted image format: jpg")
                .withCauseInstanceOf(IllegalArgumentException.class);
    }
}
