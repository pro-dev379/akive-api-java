package net.akive.api.auth.domain;

import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.auth.domain.emailverification.exception.EmailVerificationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.*;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.*;
import static net.akive.api.auth.domain.emailverification.exception.EmailVerificationErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EmailVerificationTest {

    private static final String EMAIL = "test@test.com";
    private static final String VERIFICATION_CODE = "123456";
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2025, 5, 21, 10, 0);

    @Test
    void 이메일_인증_정보를_생성한다() {
        // given
        EmailVerificationPurpose verificationPurpose = SIGN_UP;

        // when
        EmailVerification emailVerification = emailVerification(verificationPurpose);

        // then
        assertThat(emailVerification.getEmail()).isEqualTo(EMAIL);
        assertThat(emailVerification.getVerificationPurpose()).isEqualTo(verificationPurpose);
        assertThat(emailVerification.getVerificationCode()).isEqualTo(VERIFICATION_CODE);
        assertThat(emailVerification.getResendCount()).isZero();
        assertThat(emailVerification.isVerified()).isFalse();
        assertThat(emailVerification.getExpiredAt()).isEqualTo(REQUESTED_AT.plus(EXPIRE_DURATION));
        assertThat(emailVerification.getLastSentAt()).isEqualTo(REQUESTED_AT);
        assertThat(emailVerification.getCreatedAt()).isEqualTo(REQUESTED_AT);
    }

    @Test
    void 재전송_가능시간_이후에_갱신하면_새_인증정보로_갱신된다() {
        // given
        LocalDateTime secondRequestedAt = REQUESTED_AT.plus(RESEND_INTERVAL);
        EmailVerification emailVerification = emailVerification(SIGN_UP);

        // when
        emailVerification.renew("654321", secondRequestedAt);

        // then
        assertThat(emailVerification.getVerificationCode()).isEqualTo("654321");
        assertThat(emailVerification.getResendCount()).isEqualTo(1);
        assertThat(emailVerification.isVerified()).isFalse();
        assertThat(emailVerification.getExpiredAt()).isEqualTo(secondRequestedAt.plus(EXPIRE_DURATION));
        assertThat(emailVerification.getLastSentAt()).isEqualTo(secondRequestedAt);
        assertThat(emailVerification.getCreatedAt()).isEqualTo(REQUESTED_AT);
    }

    @Test
    void 재전송_가능시간_이전에_갱신하면_갱신이_거부된다() {
        // given
        EmailVerification emailVerification = emailVerification(SIGN_UP);

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() -> emailVerification.renew("654321", REQUESTED_AT.plusSeconds(10)))
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(RESEND_TOO_SOON);
    }

    @Test
    void 재전송_횟수를_초과하면_갱신이_거부된다() {
        // given
        EmailVerification emailVerification = emailVerification(SIGN_UP);

        emailVerification.renew("111111", REQUESTED_AT.plusMinutes(1));
        emailVerification.renew("222222", REQUESTED_AT.plusMinutes(2));
        emailVerification.renew("333333", REQUESTED_AT.plusMinutes(3));

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() -> emailVerification.renew("444444", REQUESTED_AT.plusMinutes(4)))
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(RESEND_LIMIT_EXCEEDED);
    }


    @Test
    void 유효한_인증번호를_입력하면_이메일_인증이_완료된다() {
        // given
        EmailVerification emailVerification = emailVerification(SIGN_UP);

        // when
        emailVerification.verify(VERIFICATION_CODE, REQUESTED_AT.plusMinutes(1));

        // then
        assertThat(emailVerification.isVerified()).isTrue();
    }

    @Test
    void 이미_인증이_완료된_인증정보이면_검증이_거부된다() {
        // given
        EmailVerification emailVerification = emailVerification(SIGN_UP);

        emailVerification.verify(VERIFICATION_CODE, REQUESTED_AT.plusMinutes(1));

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() -> emailVerification.verify(VERIFICATION_CODE, REQUESTED_AT.plusMinutes(2)))
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(ALREADY_VERIFIED);
    }

    @Test
    void 일치하지_않는_인증번호를_입력하면_검증이_실패한다() {
        // given
        EmailVerification emailVerification = emailVerification(SIGN_UP);

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() -> emailVerification.verify("000000", REQUESTED_AT.plusMinutes(1)))
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(CODE_MISMATCH);
    }

    @Test
    void 만료된_인증번호를_입력하면_검증이_실패한다() {
        // given
        EmailVerification emailVerification = emailVerification(SIGN_UP);

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() ->
                        emailVerification.verify(
                                VERIFICATION_CODE,
                                REQUESTED_AT.plus(EXPIRE_DURATION).plusSeconds(1)
                        )
                )
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(EXPIRED);
    }

    private EmailVerification emailVerification(EmailVerificationPurpose verificationPurpose) {
        return EmailVerification.create(EMAIL, verificationPurpose, VERIFICATION_CODE, REQUESTED_AT);
    }
}