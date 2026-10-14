package net.akive.api.common.id;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UuidV7GeneratorTest {

    private final UuidV7Generator generator = new UuidV7Generator();

    @Test
    void UUID_v7을_생성한다() {
        UUID uuid = generator.generate();

        assertThat(uuid.version()).isEqualTo(7);
        assertThat(uuid.variant()).isEqualTo(2);
    }
}
