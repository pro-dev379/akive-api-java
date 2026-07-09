package net.akive.api.auth.domain.login;

import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.BLANK_VALUE;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.VALUE_NOT_POSITIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LegacyUserPasswordTest {

    @Test
    void 레거시_패스워드를_생성한다() {
        // when
        LegacyUserPassword legacyUserPassword = new LegacyUserPassword(1L, "hash", "salt");

        // then
        assertThat(legacyUserPassword.userId()).isEqualTo(1L);
        assertThat(legacyUserPassword.hash()).isEqualTo("hash");
        assertThat(legacyUserPassword.salt()).isEqualTo("salt");
    }

    @Test
    void 레거시_패스워드는_회원_식별자가_없으면_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new LegacyUserPassword(null, "hash", "salt"))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 레거시_패스워드는_회원_식별자가_양수가_아니면_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new LegacyUserPassword(0L, "hash", "salt"))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NOT_POSITIVE);
    }

    @Test
    void 레거시_패스워드는_hash가_null이면_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new LegacyUserPassword(1L, null, "salt"))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 레거시_패스워드는_hash가_공백이면_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new LegacyUserPassword(1L, "   ", "salt"))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 레거시_패스워드는_salt가_null이면_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new LegacyUserPassword(1L, "hash", null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 레거시_패스워드는_salt가_공백이면_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new LegacyUserPassword(1L, "hash", "   "))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(BLANK_VALUE);
    }
}
