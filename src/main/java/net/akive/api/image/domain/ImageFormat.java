package net.akive.api.image.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ImageFormat {

    WEBP("webp");

    private final String value;

    public static ImageFormat from(String text) {
        return Arrays.stream(values())
                .filter(format -> format.value.equals(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported image format: " + text));
    }
}
