package net.akive.api.image.adapter.generation;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UuidV7ImageAssetIdGeneratorTest {

    private final UuidV7ImageAssetIdGenerator generator = new UuidV7ImageAssetIdGenerator();

    @Test
    void UUID_v7_이미지_에셋_식별자를_생성한다() {
        UUID imageAssetId = generator.generate();

        assertThat(imageAssetId.version()).isEqualTo(7);
        assertThat(imageAssetId.variant()).isEqualTo(2);
    }
}
