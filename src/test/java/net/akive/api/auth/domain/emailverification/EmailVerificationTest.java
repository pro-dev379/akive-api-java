package net.akive.api.auth.domain.emailverification;

import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.TooManyRequestsException;
import net.akive.api.common.exception.business.UnauthorizedException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static net.akive.api.auth.domain.emailverification.EmailVerificationFixture.*;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.EMAIL_VERIFICATION_RENEWAL_MIN_INTERVAL;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_EXPIRATION_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.common.domain.EmailFixture.createEmail;
import static net.akive.api.common.exception.business.BadRequestError.EMAIL_VERIFICATION_CODE_MISMATCH;
import static net.akive.api.common.exception.business.ConflictError.EMAIL_ALREADY_VERIFIED;
import static net.akive.api.common.exception.business.TooManyRequestsError.EMAIL_VERIFICATION_RENEWAL_LIMIT_EXCEEDED;
import static net.akive.api.common.exception.business.TooManyRequestsError.EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT;
import static net.akive.api.common.exception.business.UnauthorizedError.EMAIL_VERIFICATION_CODE_EXPIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EmailVerificationTest {

    @Test
    void 이메일_인증_정보를_생성한다() {
        Email email = createEmail();

        EmailVerification emailVerification = EmailVerification.create(
                email,
                SIGN_UP,
                VERIFICATION_CODE,
                REQUESTED_AT
        );

        assertThat(emailVerification.getEmail()).isEqualTo(email);
        assertThat(emailVerification.getVerificationPurpose()).isEqualTo(SIGN_UP);
        assertThat(emailVerification.getVerificationCode()).isEqualTo(VERIFICATION_CODE);
        assertThat(emailVerification.getRenewalCount()).isZero();
        assertThat(emailVerification.isVerified()).isFalse();
        assertThat(emailVerification.getExpiresAt())
                .isEqualTo(REQUESTED_AT.plus(VERIFICATION_CODE_EXPIRATION_DURATION));
        assertThat(emailVerification.getLastSentAt()).isEqualTo(REQUESTED_AT);
        assertThat(emailVerification.getCreatedAt()).isEqualTo(REQUESTED_AT);
    }

    @Test
    void 새_인증정보로_갱신한다() {
        EmailVerification emailVerification = createEmailVerification();

        LocalDateTime secondRequestedAt = REQUESTED_AT.plus(EMAIL_VERIFICATION_RENEWAL_MIN_INTERVAL);
        EmailVerificationCode newCode = new EmailVerificationCode("111111");

        emailVerification.renew(newCode, secondRequestedAt);

        assertThat(emailVerification.getVerificationCode()).isEqualTo(newCode);
        assertThat(emailVerification.isVerified()).isFalse();
        assertThat(emailVerification.getRenewalCount()).isEqualTo(1);
        assertThat(emailVerification.getExpiresAt())
                .isEqualTo(secondRequestedAt.plus(VERIFICATION_CODE_EXPIRATION_DURATION));
        assertThat(emailVerification.getLastSentAt()).isEqualTo(secondRequestedAt);
    }

    @Test
    void 갱신_가능시간_이전에_갱신하면_갱신이_거부된다() {
        EmailVerification emailVerification = createEmailVerification();

        assertThatExceptionOfType(TooManyRequestsException.class)
                .isThrownBy(() -> emailVerification.renew(
                        VERIFICATION_CODE,
                        REQUESTED_AT.plusSeconds(10)
                ))
                .extracting(TooManyRequestsException::getError)
                .isEqualTo(EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT);
    }

    @Test
    void 갱신_횟수를_초과하면_갱신이_거부된다() {
        EmailVerification emailVerification = createEmailVerification();

        emailVerification.renew(new EmailVerificationCode("111111"), REQUESTED_AT.plusMinutes(1));
        emailVerification.renew(new EmailVerificationCode("222222"), REQUESTED_AT.plusMinutes(2));
        emailVerification.renew(new EmailVerificationCode("333333"), REQUESTED_AT.plusMinutes(3));

        assertThatExceptionOfType(TooManyRequestsException.class)
                .isThrownBy(() -> emailVerification.renew(
                        new EmailVerificationCode("444444"),
                        REQUESTED_AT.plusMinutes(4)
                ))
                .extracting(TooManyRequestsException::getError)
                .isEqualTo(EMAIL_VERIFICATION_RENEWAL_LIMIT_EXCEEDED);
    }

    @Test
    void 이메일_인증한다() {
        EmailVerification emailVerification = createEmailVerification();

        emailVerification.verify(VERIFICATION_CODE, REQUESTED_AT.plusMinutes(1));

        assertThat(emailVerification.isVerified()).isTrue();
    }

    @Test
    void 만료_시각과_같은_시각에_인증번호를_입력해도_인증_성공한다() {
        EmailVerification emailVerification = createEmailVerification();

        emailVerification.verify(VERIFICATION_CODE, REQUESTED_AT.plus(VERIFICATION_CODE_EXPIRATION_DURATION));

        assertThat(emailVerification.isVerified()).isTrue();
    }

    @Test
    void 이미_인증이_완료된_경우_인증에_실패한다() {
        EmailVerification emailVerification = createEmailVerification();

        emailVerification.verify(VERIFICATION_CODE, REQUESTED_AT.plusMinutes(1));

        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> emailVerification.verify(
                        VERIFICATION_CODE,
                        REQUESTED_AT.plusMinutes(2)
                ))
                .extracting(ConflictException::getError)
                .isEqualTo(EMAIL_ALREADY_VERIFIED);
    }

    @Test
    void 일치하지_않는_인증번호를_입력하면_인증에_실패한다() {
        EmailVerification emailVerification = createEmailVerification();

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> emailVerification.verify(
                        new EmailVerificationCode("000000"),
                        REQUESTED_AT.plusMinutes(1)
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(EMAIL_VERIFICATION_CODE_MISMATCH);
    }

    @Test
    void 만료된_인증번호를_입력하면_인증에_실패한다() {
        EmailVerification emailVerification = createEmailVerification();

        assertThatExceptionOfType(UnauthorizedException.class)
                .isThrownBy(() -> emailVerification.verify(
                        VERIFICATION_CODE,
                        REQUESTED_AT.plus(VERIFICATION_CODE_EXPIRATION_DURATION).plusSeconds(1)
                ))
                .extracting(UnauthorizedException::getError)
                .isEqualTo(EMAIL_VERIFICATION_CODE_EXPIRED);
    }
}
