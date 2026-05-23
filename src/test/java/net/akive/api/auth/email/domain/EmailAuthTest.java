package net.akive.api.auth.email.domain;

import net.akive.api.auth.email.domain.exception.EmailAuthResendLimitExceededException;
import net.akive.api.auth.email.domain.exception.EmailAuthResendTooSoonException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static net.akive.api.auth.email.domain.EmailAuthPolicy.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailAuthTest {

    @Test
    void 이메일_인증_정보를_생성한다() {
        // given
        String email = "test@test.com";
        EmailAuthPurpose authPurpose = EmailAuthPurpose.JOIN;
        String authCode = "123456";
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);

        // when
        EmailAuth emailAuth = EmailAuth.create(email, authPurpose, authCode, requestedAt);

        // then
        assertThat(emailAuth.getAuthCode()).isEqualTo(authCode);
        assertThat(emailAuth.getResendCount()).isZero();
        assertThat(emailAuth.isVerified()).isFalse();
        assertThat(emailAuth.getExpiredAt()).isEqualTo(requestedAt.plus(EXPIRE_DURATION));
        assertThat(emailAuth.getLastSentAt()).isEqualTo(requestedAt);
        assertThat(emailAuth.getCreatedAt()).isEqualTo(requestedAt);
    }

    @Test
    void 이메일_인증_정보를_갱신한다() {
        // given
        LocalDateTime firstRequestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);
        LocalDateTime secondRequestedAt = firstRequestedAt.plus(RESEND_INTERVAL);

        EmailAuth emailAuth = EmailAuth.create("test@test.com", EmailAuthPurpose.JOIN, "123456", firstRequestedAt);

        // when
        emailAuth.renew("654321", secondRequestedAt);

        // then
        assertThat(emailAuth.getAuthCode()).isEqualTo("654321");
        assertThat(emailAuth.getResendCount()).isEqualTo(1);
        assertThat(emailAuth.isVerified()).isFalse();
        assertThat(emailAuth.getExpiredAt()).isEqualTo(secondRequestedAt.plus(EXPIRE_DURATION));
        assertThat(emailAuth.getLastSentAt()).isEqualTo(secondRequestedAt);
        assertThat(emailAuth.getCreatedAt()).isEqualTo(firstRequestedAt);
    }

    @Test
    void 재전송_가능_시간_이전에_갱신하면_예외가_발생한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);
        EmailAuth emailAuth = EmailAuth.create("test@test.com", EmailAuthPurpose.JOIN, "123456", requestedAt);

        // when & then
        assertThatThrownBy(() -> emailAuth.renew("654321", requestedAt.plusSeconds(10)))
                .isInstanceOf(EmailAuthResendTooSoonException.class);
    }

    @Test
    void 재전송_최대_횟수를_초과하면_예외가_발생한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);
        EmailAuth emailAuth = EmailAuth.create("test@test.com", EmailAuthPurpose.JOIN, "123456", requestedAt);

        emailAuth.renew("111111", requestedAt.plusMinutes(1));
        emailAuth.renew("222222", requestedAt.plusMinutes(2));
        emailAuth.renew("333333", requestedAt.plusMinutes(3));

        // when & then
        assertThatThrownBy(() -> emailAuth.renew("444444", requestedAt.plusMinutes(4)))
                .isInstanceOf(EmailAuthResendLimitExceededException.class);
    }
}