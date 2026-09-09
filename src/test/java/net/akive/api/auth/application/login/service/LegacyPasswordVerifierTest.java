package net.akive.api.auth.application.login.service;

import net.akive.api.auth.domain.login.LegacyPasswordHash;
import org.junit.jupiter.api.Test;

import static net.akive.api.auth.domain.login.LegacyUserPasswordFixture.createLegacyPasswordHash;
import static net.akive.api.user.domain.fixture.UserFixture.PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;

class LegacyPasswordVerifierTest {

    private final LegacyPasswordVerifier legacyPasswordVerifier = new LegacyPasswordVerifier();

    @Test
    void 레거시_패스워드_검증에_성공한다() {
        LegacyPasswordHash passwordHash = createLegacyPasswordHash();

        boolean result = legacyPasswordVerifier.matches(PASSWORD, passwordHash);

        assertThat(result).isTrue();
    }

    @Test
    void 기존_레거시_패스워드와_일치하지_않아_검증에_실패한다() {
        LegacyPasswordHash passwordHash = createLegacyPasswordHash();

        boolean result = legacyPasswordVerifier.matches("wrong-password", passwordHash);

        assertThat(result).isFalse();
    }
}
