package net.akive.api.file.adapter.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.file.domain.ImageFormat;

@Converter
public class ImageFormatConverter implements AttributeConverter<ImageFormat, String> {

    @Override
    public String convertToDatabaseColumn(ImageFormat attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ImageFormat convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            throw new InvalidPersistedDataException("Persisted image format must not be null.");
        }

        try {
            return ImageFormat.from(dbData);
        } catch (IllegalArgumentException e) {
            throw new InvalidPersistedDataException("Unsupported persisted image format: " + dbData, e);
        }
    }
}
