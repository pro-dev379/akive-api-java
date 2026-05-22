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
        String authCode = "123456";
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);

        // when
        EmailAuth emailAuth = EmailAuth.create(authCode, requestedAt);

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

        EmailAuth emailAuth = EmailAuth.create("123456", firstRequestedAt);

        // when
        EmailAuth renewedEmailAuth = emailAuth.renew("654321", secondRequestedAt);

        // then
        assertThat(renewedEmailAuth.getAuthCode()).isEqualTo("654321");
        assertThat(renewedEmailAuth.getResendCount()).isEqualTo(1);
        assertThat(renewedEmailAuth.isVerified()).isFalse();
        assertThat(renewedEmailAuth.getExpiredAt()).isEqualTo(secondRequestedAt.plus(EXPIRE_DURATION));
        assertThat(renewedEmailAuth.getLastSentAt()).isEqualTo(secondRequestedAt);
        assertThat(renewedEmailAuth.getCreatedAt()).isEqualTo(firstRequestedAt);
    }

    @Test
    void 재전송_가능_시간_이전에_갱신하면_예외가_발생한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);
        EmailAuth emailAuth = EmailAuth.create("123456", requestedAt);

        // when & then
        assertThatThrownBy(() -> emailAuth.renew("654321", requestedAt.plusSeconds(10)))
                .isInstanceOf(EmailAuthResendTooSoonException.class);
    }

    @Test
    void 재전송_최대_횟수를_초과하면_예외가_발생한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);

        EmailAuth emailAuth = new EmailAuth(
                "123456",
                MAX_RESEND_COUNT,
                false,
                requestedAt.plus(EXPIRE_DURATION),
                requestedAt.minus(RESEND_INTERVAL),
                requestedAt.minus(RESEND_INTERVAL)
        );

        // when & then
        assertThatThrownBy(() -> emailAuth.renew("654321", requestedAt))
                .isInstanceOf(EmailAuthResendLimitExceededException.class);
    }
}