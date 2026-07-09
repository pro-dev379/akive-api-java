package net.akive.api.auth.domain.token;

import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.VALUE_NOT_POSITIVE;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TokenPayloadTest {

    @Test
    void 토큰_페이로드를_생성한다() {
        // given
        Instant expiresAt = Instant.parse("2026-07-08T00:00:00Z");

        // when
        TokenPayload tokenPayload = new TokenPayload(1L, USER, expiresAt);

        // then
        assertThat(tokenPayload.userId()).isEqualTo(1L);
        assertThat(tokenPayload.roleType()).isEqualTo(USER);
        assertThat(tokenPayload.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void 토큰_payload는_회원_식별값이_없으면_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new TokenPayload(null, USER, Instant.parse("2026-07-08T00:00:00Z")))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 토큰_payload는_회원_식별값이_양수가_아니면_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new TokenPayload(0L, USER, Instant.parse("2026-07-08T00:00:00Z")))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NOT_POSITIVE);
    }

    @Test
    void 토큰_payload는_역할이_없으면_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new TokenPayload(1L, null, Instant.parse("2026-07-08T00:00:00Z")))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 토큰_payload는_만료_시각이_없으면_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new TokenPayload(1L, USER, null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }
}
