package net.akive.api.auth.domain;

import net.akive.api.auth.domain.exception.EmailVerificationException;
import net.akive.api.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static net.akive.api.auth.domain.EmailVerificationPolicy.*;
import static net.akive.api.auth.domain.EmailVerificationPurpose.*;
import static net.akive.api.auth.domain.exception.EmailVerificationErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailVerificationTest {

    @Test
    void 이메일_인증_정보를_생성한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        String verificationCode = "123456";
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);

        // when
        EmailVerification emailVerification = EmailVerification.create(email, verificationPurpose, verificationCode, requestedAt);

        // then
        assertThat(emailVerification.getVerificationCode()).isEqualTo(verificationCode);
        assertThat(emailVerification.getResendCount()).isZero();
        assertThat(emailVerification.isVerified()).isFalse();
        assertThat(emailVerification.getExpiredAt()).isEqualTo(requestedAt.plus(EXPIRE_DURATION));
        assertThat(emailVerification.getLastSentAt()).isEqualTo(requestedAt);
        assertThat(emailVerification.getCreatedAt()).isEqualTo(requestedAt);
    }

    @Test
    void 재전송_가능시간_이후에_갱신하면_새_인증정보로_갱신된다() {
        // given
        LocalDateTime firstRequestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);
        LocalDateTime secondRequestedAt = firstRequestedAt.plus(RESEND_INTERVAL);
        EmailVerification emailVerification = EmailVerification.create("test@test.com", SIGN_UP, "123456", firstRequestedAt);

        // when
        emailVerification.renew("654321", secondRequestedAt);

        // then
        assertThat(emailVerification.getVerificationCode()).isEqualTo("654321");
        assertThat(emailVerification.getResendCount()).isEqualTo(1);
        assertThat(emailVerification.isVerified()).isFalse();
        assertThat(emailVerification.getExpiredAt()).isEqualTo(secondRequestedAt.plus(EXPIRE_DURATION));
        assertThat(emailVerification.getLastSentAt()).isEqualTo(secondRequestedAt);
        assertThat(emailVerification.getCreatedAt()).isEqualTo(firstRequestedAt);
    }

    @Test
    void 재전송_가능시간_이전에_갱신하면_예외가_발생한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);
        EmailVerification emailVerification = EmailVerification.create("test@test.com", SIGN_UP, "123456", requestedAt);

        // when & then
        assertThatThrownBy(() -> emailVerification.renew("654321", requestedAt.plusSeconds(10)))
                .satisfies(e -> assertErrorCode(e, RESEND_TOO_SOON));
    }

    @Test
    void 재전송_횟수를_초과하면_예외가_발생한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);
        EmailVerification emailVerification = EmailVerification.create("test@test.com", SIGN_UP, "123456", requestedAt);

        emailVerification.renew("111111", requestedAt.plusMinutes(1));
        emailVerification.renew("222222", requestedAt.plusMinutes(2));
        emailVerification.renew("333333", requestedAt.plusMinutes(3));

        // when & then
        assertThatThrownBy(() -> emailVerification.renew("444444", requestedAt.plusMinutes(4)))
                .satisfies(e -> assertErrorCode(e, RESEND_LIMIT_EXCEEDED));
    }


    @Test
    void 유효한_인증번호를_입력하면_이메일_인증이_완료된다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);
        String verificationCode = "123456";
        EmailVerification emailVerification = EmailVerification.create(
                "test@test.com",
                SIGN_UP,
                verificationCode,
                requestedAt
        );

        // when
        emailVerification.verify(verificationCode, requestedAt.plusMinutes(1));

        // then
        assertThat(emailVerification.isVerified()).isTrue();
    }

    @Test
    void 이미_인증이_완료된_경우_예외가_발생한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);

        EmailVerification emailVerification = EmailVerification.create(
                "test@test.com",
                SIGN_UP,
                "123456",
                requestedAt
        );

        emailVerification.verify("123456", requestedAt.plusMinutes(1));

        // when & then
        assertThatThrownBy(() -> emailVerification.verify("123456", requestedAt.plusMinutes(2)))
                .satisfies(e -> assertErrorCode(e, ALREADY_VERIFIED));
    }

    @Test
    void 인증번호가_일치하지_않으면_예외가_발생한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);

        EmailVerification emailVerification = EmailVerification.create(
                "test@test.com",
                SIGN_UP,
                "123456",
                requestedAt
        );

        // when & then
        assertThatThrownBy(() -> emailVerification.verify("000000", requestedAt.plusMinutes(1)))
                .satisfies(e -> assertErrorCode(e, CODE_MISMATCH));
    }

    @Test
    void 인증번호가_만료되면_예외가_발생한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);

        EmailVerification emailVerification = EmailVerification.create(
                "test@test.com",
                SIGN_UP,
                "123456",
                requestedAt
        );

        // when & then
        assertThatThrownBy(() -> emailVerification.verify("123456", requestedAt.plus(EXPIRE_DURATION).plusSeconds(1)))
                .satisfies(e -> assertErrorCode(e, EXPIRED));
    }

    private void assertErrorCode(Throwable throwable, ErrorCode expected) {
        assertThat(throwable)
                .isInstanceOf(EmailVerificationException.class);

        assertThat(((EmailVerificationException) throwable).getErrorCode())
                .isEqualTo(expected);
    }
}