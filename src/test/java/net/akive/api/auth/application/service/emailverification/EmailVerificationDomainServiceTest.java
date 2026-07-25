package net.akive.api.auth.application.service.emailverification;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.common.exception.business.TooManyRequestsException;
import net.akive.api.common.exception.business.UnauthorizedException;

import net.akive.api.common.domain.Email;
import net.akive.api.auth.application.port.out.emailverification.GenerateEmailVerificationCodePort;
import net.akive.api.auth.application.port.out.emailverification.LoadEmailVerificationPort;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.common.time.FixedCurrentTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.EXPIRE_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.common.exception.business.BadRequestError.*;
import static net.akive.api.common.exception.business.ConflictError.*;
import static net.akive.api.common.exception.business.NotFoundError.*;
import static net.akive.api.common.exception.business.TooManyRequestsError.*;
import static net.akive.api.common.exception.business.UnauthorizedError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EmailVerificationDomainServiceTest {

    private static final String EMAIL = "test@test.com";
    private static final EmailVerificationCode VERIFICATION_CODE = new EmailVerificationCode("123456");

    @Mock
    EmailVerificationRequestValidator emailVerificationRequestValidator;

    @Mock
    GenerateEmailVerificationCodePort generateEmailVerificationCodePort;

    @Mock
    LoadEmailVerificationPort loadEmailVerificationPort;

    CurrentTimeProvider currentTimeProvider;

    EmailVerificationDomainService emailVerificationDomainService;

    @BeforeEach
    void setUp() {
        currentTimeProvider = new FixedCurrentTimeProvider(Instant.parse("2025-05-21T01:00:00Z"));
        emailVerificationDomainService = new EmailVerificationDomainService(
                emailVerificationRequestValidator,
                generateEmailVerificationCodePort,
                loadEmailVerificationPort,
                currentTimeProvider
        );
    }

    @Test
    void 새로운_이메일_인증번호를_발급한다() {
        // given
        given(generateEmailVerificationCodePort.generate()).willReturn(VERIFICATION_CODE);
        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(new Email(EMAIL), SIGN_UP))
                .willReturn(Optional.empty());

        // when
        EmailVerification emailVerification = emailVerificationDomainService.issue(new Email(EMAIL), SIGN_UP);

        // then
        assertThat(emailVerification.getEmail()).isEqualTo(new Email(EMAIL));
        assertThat(emailVerification.getVerificationPurpose()).isEqualTo(SIGN_UP);
        assertThat(emailVerification.getVerificationCode()).isEqualTo(VERIFICATION_CODE);
        assertThat(emailVerification.isVerified()).isFalse();
        assertThat(emailVerification.getResendCount()).isZero();
        assertThat(emailVerification.getLastSentAt()).isEqualTo(currentTimeProvider.now());
        assertThat(emailVerification.getExpiresAt()).isEqualTo(currentTimeProvider.now().plus(EXPIRE_DURATION));

        then(emailVerificationRequestValidator).should().validateRequestAvailable(new Email(EMAIL), SIGN_UP);
    }

    @Test
    void 기존_인증정보가_존재하면_새_인증번호로_갱신한다() {
        // given
        EmailVerification previous = emailVerification(SIGN_UP, LocalDateTime.of(2025, 5, 21, 9, 55));

        given(generateEmailVerificationCodePort.generate()).willReturn(VERIFICATION_CODE);
        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(new Email(EMAIL), SIGN_UP))
                .willReturn(Optional.of(previous));

        // when
        EmailVerification emailVerification = emailVerificationDomainService.issue(new Email(EMAIL), SIGN_UP);

        // then
        assertThat(emailVerification.getVerificationCode()).isEqualTo(VERIFICATION_CODE);
        assertThat(emailVerification.getResendCount()).isEqualTo(1);
        assertThat(emailVerification.isVerified()).isFalse();
        assertThat(emailVerification.getLastSentAt()).isEqualTo(currentTimeProvider.now());
        assertThat(emailVerification.getExpiresAt()).isEqualTo(currentTimeProvider.now().plus(EXPIRE_DURATION));
    }

    @Test
    void 재전송_가능시간_이전에_인증번호를_요청하면_재전송이_거부된다() {
        // given
        given(generateEmailVerificationCodePort.generate()).willReturn(VERIFICATION_CODE);
        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(new Email(EMAIL), SIGN_UP))
                .willReturn(Optional.of(emailVerification(SIGN_UP, currentTimeProvider.now())));

        // when & then
        assertThatExceptionOfType(TooManyRequestsException.class)
                .isThrownBy(() -> emailVerificationDomainService.issue(new Email(EMAIL), SIGN_UP))
                .extracting(TooManyRequestsException::getError)
                .isEqualTo(EMAIL_VERIFICATION_REQUEST_TOO_FREQUENT);
    }

    @Test
    void 재전송_횟수를_초과하면_인증번호_발급이_거부된다() {
        // given
        LocalDateTime requestedAt = currentTimeProvider.now();
        EmailVerification emailVerification = emailVerification(SIGN_UP, requestedAt);
        emailVerification.renew(new EmailVerificationCode("111111"), requestedAt.plusMinutes(1));
        emailVerification.renew(new EmailVerificationCode("222222"), requestedAt.plusMinutes(2));
        emailVerification.renew(new EmailVerificationCode("333333"), requestedAt.plusMinutes(3));

        given(generateEmailVerificationCodePort.generate()).willReturn(VERIFICATION_CODE);
        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(new Email(EMAIL), SIGN_UP))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatExceptionOfType(TooManyRequestsException.class)
                .isThrownBy(() -> emailVerificationDomainService.issue(new Email(EMAIL), SIGN_UP))
                .extracting(TooManyRequestsException::getError)
                .isEqualTo(EMAIL_VERIFICATION_REQUEST_LIMIT_EXCEEDED);
    }

    @Test
    void 유효한_인증번호를_입력하면_이메일_인증을_완료한다() {
        // given
        EmailVerification emailVerification = emailVerification(SIGN_UP, currentTimeProvider.now().minusMinutes(1));

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(new Email(EMAIL), SIGN_UP))
                .willReturn(Optional.of(emailVerification));

        // when
        EmailVerification verifiedEmailVerification = emailVerificationDomainService.verify(new Email(EMAIL), SIGN_UP, VERIFICATION_CODE);

        // then
        assertThat(verifiedEmailVerification.isVerified()).isTrue();
    }

    @Test
    void 인증정보가_존재하지_않으면_인증번호_검증이_실패한다() {
        // given
        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(new Email(EMAIL), SIGN_UP))
                .willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> emailVerificationDomainService.verify(new Email(EMAIL), SIGN_UP, VERIFICATION_CODE))
                .extracting(NotFoundException::getError)
                .isEqualTo(EMAIL_VERIFICATION_NOT_FOUND);
    }

    @Test
    void 일치하지_않는_인증번호를_입력하면_인증번호_검증이_실패한다() {
        // given
        EmailVerification emailVerification = emailVerification(SIGN_UP, currentTimeProvider.now().minusMinutes(1));

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(new Email(EMAIL), SIGN_UP))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> emailVerificationDomainService.verify(
                        new Email(EMAIL),
                        SIGN_UP,
                        new EmailVerificationCode("000000")
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(EMAIL_VERIFICATION_CODE_MISMATCH);
    }

    @Test
    void 만료된_인증번호를_입력하면_인증번호_검증이_실패한다() {
        // given
        EmailVerification emailVerification = emailVerification(
                SIGN_UP,
                currentTimeProvider.now().minus(EXPIRE_DURATION).minusSeconds(1)
        );

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(new Email(EMAIL), SIGN_UP))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatExceptionOfType(UnauthorizedException.class)
                .isThrownBy(() -> emailVerificationDomainService.verify(new Email(EMAIL), SIGN_UP, VERIFICATION_CODE))
                .extracting(UnauthorizedException::getError)
                .isEqualTo(EXPIRED_EMAIL_VERIFICATION_CODE);
    }

    @Test
    void 이미_인증이_완료된_인증정보이면_인증번호_검증이_거부된다() {
        // given
        EmailVerification emailVerification = emailVerification(SIGN_UP, currentTimeProvider.now().minusMinutes(2));
        emailVerification.verify(VERIFICATION_CODE, currentTimeProvider.now().minusMinutes(1));

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(new Email(EMAIL), SIGN_UP))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> emailVerificationDomainService.verify(new Email(EMAIL), SIGN_UP, VERIFICATION_CODE))
                .extracting(ConflictException::getError)
                .isEqualTo(EMAIL_VERIFICATION_ALREADY_VERIFIED);
    }

    @Test
    void 인증이_완료된_인증정보이면_인증_완료_확인에_성공한다() {
        // given
        EmailVerification emailVerification = emailVerification(SIGN_UP, currentTimeProvider.now().minusMinutes(2));
        emailVerification.verify(VERIFICATION_CODE, currentTimeProvider.now().minusMinutes(1));

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(new Email(EMAIL), SIGN_UP))
                .willReturn(Optional.of(emailVerification));

        // when
        emailVerificationDomainService.ensureVerified(new Email(EMAIL), SIGN_UP);

        // then
        then(loadEmailVerificationPort).should().loadByEmailAndVerificationPurpose(new Email(EMAIL), SIGN_UP);
    }

    @Test
    void 인증정보가_존재하지_않으면_인증_완료_확인이_실패한다() {
        // given
        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(new Email(EMAIL), SIGN_UP))
                .willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> emailVerificationDomainService.ensureVerified(new Email(EMAIL), SIGN_UP))
                .extracting(BadRequestException::getError)
                .isEqualTo(EMAIL_VERIFICATION_NOT_VERIFIED);
    }

    @Test
    void 인증이_완료되지_않은_인증정보이면_인증_완료_확인이_실패한다() {
        // given
        EmailVerification emailVerification = emailVerification(SIGN_UP, currentTimeProvider.now().minusMinutes(1));

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(new Email(EMAIL), SIGN_UP))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> emailVerificationDomainService.ensureVerified(new Email(EMAIL), SIGN_UP))
                .extracting(BadRequestException::getError)
                .isEqualTo(EMAIL_VERIFICATION_NOT_VERIFIED);
    }

    private EmailVerification emailVerification(
            EmailVerificationPurpose verificationPurpose,
            LocalDateTime requestedAt
    ) {
        return EmailVerification.create(new Email(EMAIL), verificationPurpose, VERIFICATION_CODE, requestedAt);
    }
}
