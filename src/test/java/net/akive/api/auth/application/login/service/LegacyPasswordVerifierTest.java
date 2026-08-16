package net.akive.api.auth.application.login.service;

import net.akive.api.auth.domain.login.LegacyPasswordHash;
import org.junit.jupiter.api.Test;

import static net.akive.api.auth.domain.login.LegacyUserPasswordFixture.RAW_PASSWORD;
import static net.akive.api.auth.domain.login.LegacyUserPasswordFixture.createLegacyPasswordHash;
import static org.assertj.core.api.Assertions.assertThat;

class LegacyPasswordVerifierTest {

    private static final String OTHER_RAW_PASSWORD = "wrong-password";

    private final LegacyPasswordVerifier legacyPasswordVerifier = new LegacyPasswordVerifier();

    @Test
    void 레거시_패스워드_검증에_성공한다() {
        LegacyPasswordHash passwordHash = createLegacyPasswordHash();

        boolean result = legacyPasswordVerifier.matches(RAW_PASSWORD, passwordHash);

        assertThat(result).isTrue();
    }

    @Test
    void 기존_레거시_패스워드와_일치하지_않아_검증에_실패한다() {
        LegacyPasswordHash passwordHash = createLegacyPasswordHash();

        boolean result = legacyPasswordVerifier.matches(OTHER_RAW_PASSWORD, passwordHash);

        assertThat(result).isFalse();
    }
}
