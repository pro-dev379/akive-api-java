package net.akive.api.auth.application;

import net.akive.api.auth.application.dto.command.emailverification.EmailVerificationCodeSendCommand;
import net.akive.api.auth.application.dto.command.emailverification.EmailVerificationCodeVerifyCommand;
import net.akive.api.auth.application.dto.command.emailverification.EmailVerificationEnsureVerifiedCommand;
import net.akive.api.auth.application.dto.result.emailverification.EmailVerificationCodeSendResult;
import net.akive.api.auth.application.port.out.emailverification.*;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.auth.domain.emailverification.exception.EmailVerificationException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.common.time.FixedCurrentTimeProvider;
import net.akive.api.user.application.port.in.ValidateUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.EXPIRE_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.RESEND_INTERVAL;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFIED_EXPIRATION_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.*;
import static net.akive.api.auth.domain.emailverification.exception.EmailVerificationErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    private static final String EMAIL = "test@test.com";
    private static final String VERIFICATION_CODE = "123456";
    private static final Instant FIXED_INSTANT = Instant.parse("2025-05-21T01:00:00Z");

    @Mock
    SendEmailVerificationMailPort sendEmailVerificationMailPort;

    @Mock
    GenerateEmailVerificationCodePort generateEmailVerificationCodePort;

    @Mock
    ValidateUserUseCase validateUserUseCase;

    @Mock
    LoadEmailVerificationPort loadEmailVerificationPort;

    @Mock
    SaveEmailVerificationPort saveEmailVerificationPort;

    CurrentTimeProvider currentTimeProvider;

    EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        currentTimeProvider = new FixedCurrentTimeProvider(FIXED_INSTANT);

        emailVerificationService = new EmailVerificationService(
                validateUserUseCase,
                sendEmailVerificationMailPort,
                generateEmailVerificationCodePort,
                loadEmailVerificationPort,
                saveEmailVerificationPort,
                currentTimeProvider
        );
    }

    @Test
    void 회원가입_이메일_인증번호를_전송한다() {
        // given
        EmailVerificationPurpose verificationPurpose = SIGN_UP;

        given(generateEmailVerificationCodePort.generate()).willReturn(VERIFICATION_CODE);
        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(EMAIL, verificationPurpose))
                .willReturn(Optional.empty());

        // when
        EmailVerificationCodeSendResult result = emailVerificationService.send(sendCommand(verificationPurpose));

        // then
        then(validateUserUseCase)
                .should()
                .validateEmailNotDuplicate(EMAIL);

        then(validateUserUseCase)
                .should(never())
                .validateUserExistsByEmail(any());

        then(sendEmailVerificationMailPort)
                .should()
                .send(EMAIL, VERIFICATION_CODE, EXPIRE_DURATION);

        ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);

        then(saveEmailVerificationPort)
                .should()
                .save(captor.capture(), eq(EXPIRE_DURATION));

        EmailVerification savedEmailVerification = captor.getValue();

        assertThat(savedEmailVerification.getEmail()).isEqualTo(EMAIL);
        assertThat(savedEmailVerification.getVerificationPurpose()).isEqualTo(verificationPurpose);
        assertThat(savedEmailVerification.getVerificationCode()).isEqualTo(VERIFICATION_CODE);
        assertThat(savedEmailVerification.isVerified()).isFalse();
        assertThat(savedEmailVerification.getResendCount()).isZero();
        assertThat(savedEmailVerification.getLastSentAt()).isEqualTo(currentTimeProvider.now());
        assertThat(savedEmailVerification.getExpiredAt())
                .isEqualTo(currentTimeProvider.now().plus(EXPIRE_DURATION));

        assertThat(result.email()).isEqualTo(EMAIL);
        assertThat(result.expiredAt()).isEqualTo(currentTimeProvider.now().plus(EXPIRE_DURATION));
        assertThat(result.expireSeconds()).isEqualTo(EXPIRE_DURATION.toSeconds());
        assertThat(result.resendIntervalSeconds()).isEqualTo(RESEND_INTERVAL.toSeconds());
    }

    @Test
    void 패스워드_찾기_이메일_인증번호를_전송한다() {
        // given
        EmailVerificationPurpose verificationPurpose = FIND_PASSWORD;

        given(generateEmailVerificationCodePort.generate()).willReturn(VERIFICATION_CODE);
        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(EMAIL, verificationPurpose))
                .willReturn(Optional.empty());

        // when
        EmailVerificationCodeSendResult result = emailVerificationService.send(sendCommand(verificationPurpose));

        // then
        then(validateUserUseCase)
                .should()
                .validateUserExistsByEmail(EMAIL);

        then(validateUserUseCase)
                .should(never())
                .validateEmailNotDuplicate(any());

        then(sendEmailVerificationMailPort)
                .should()
                .send(EMAIL, VERIFICATION_CODE, EXPIRE_DURATION);

        then(saveEmailVerificationPort)
                .should()
                .save(any(EmailVerification.class), eq(EXPIRE_DURATION));

        assertThat(result.email()).isEqualTo(EMAIL);
        assertThat(result.expiredAt()).isEqualTo(currentTimeProvider.now().plus(EXPIRE_DURATION));
        assertThat(result.expireSeconds()).isEqualTo(EXPIRE_DURATION.toSeconds());
        assertThat(result.resendIntervalSeconds()).isEqualTo(RESEND_INTERVAL.toSeconds());
    }

    @Test
    void 기존_인증정보가_존재하면_새_인증번호로_갱신하여_전송한다() {
        // given
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        EmailVerification previous = EmailVerification.create(
                EMAIL,
                verificationPurpose,
                "000000",
                LocalDateTime.of(2025, 5, 21, 9, 55)
        );

        given(generateEmailVerificationCodePort.generate()).willReturn(VERIFICATION_CODE);
        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(EMAIL, verificationPurpose))
                .willReturn(Optional.of(previous));

        // when
        emailVerificationService.send(sendCommand(verificationPurpose));

        // then
        then(sendEmailVerificationMailPort)
                .should()
                .send(EMAIL, VERIFICATION_CODE, EXPIRE_DURATION);

        ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);

        then(saveEmailVerificationPort)
                .should()
                .save(captor.capture(), eq(EXPIRE_DURATION));

        EmailVerification savedEmailVerification = captor.getValue();

        assertThat(savedEmailVerification.getVerificationCode()).isEqualTo(VERIFICATION_CODE);
        assertThat(savedEmailVerification.getResendCount()).isEqualTo(1);
        assertThat(savedEmailVerification.isVerified()).isFalse();
        assertThat(savedEmailVerification.getLastSentAt()).isEqualTo(currentTimeProvider.now());
        assertThat(savedEmailVerification.getExpiredAt())
                .isEqualTo(currentTimeProvider.now().plus(EXPIRE_DURATION));
    }

    @Test
    void 재전송_가능시간_이전에_인증번호를_요청하면_재전송이_거부된다() {
        // given
        EmailVerificationPurpose verificationPurpose = SIGN_UP;

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(EMAIL, verificationPurpose))
                .willReturn(
                        Optional.of(emailVerification(verificationPurpose, currentTimeProvider.now()))
                );

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() -> emailVerificationService.send(sendCommand(verificationPurpose)))
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(RESEND_TOO_SOON);

        then(saveEmailVerificationPort)
                .should(never())
                .save(any(), any());

        then(sendEmailVerificationMailPort)
                .should(never())
                .send(any(), any(), any());
    }

    @Test
    void 재전송_횟수를_초과하면_인증번호_전송이_거부된다() {
        // given
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        LocalDateTime requestedAt = currentTimeProvider.now();
        EmailVerification emailVerification = emailVerification(verificationPurpose, requestedAt);

        emailVerification.renew("111111", requestedAt.plusMinutes(1));
        emailVerification.renew("222222", requestedAt.plusMinutes(2));
        emailVerification.renew("333333", requestedAt.plusMinutes(3));

        given(generateEmailVerificationCodePort.generate()).willReturn(VERIFICATION_CODE);
        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(EMAIL, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() -> emailVerificationService.send(sendCommand(verificationPurpose)))
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(RESEND_LIMIT_EXCEEDED);

        then(sendEmailVerificationMailPort)
                .should(never())
                .send(any(), any(), any());

        then(saveEmailVerificationPort)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 유효한_인증번호를_입력하면_이메일_인증을_완료한다() {
        // given
        EmailVerificationPurpose verificationPurpose = SIGN_UP;

        LocalDateTime requestedAt = currentTimeProvider.now().minusMinutes(1);
        EmailVerification emailVerification = emailVerification(verificationPurpose, requestedAt);

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(EMAIL, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when
        emailVerificationService.verify(verifyCommand(verificationPurpose, VERIFICATION_CODE));

        // then
        then(saveEmailVerificationPort)
                .should()
                .save(
                        argThat(savedEmailVerification ->
                                savedEmailVerification.isVerified()
                                        && savedEmailVerification.getEmail().equals(EMAIL)
                                        && savedEmailVerification.getVerificationPurpose() == verificationPurpose
                        ),
                        eq(VERIFIED_EXPIRATION_DURATION)
                );
    }

    @Test
    void 인증정보가_존재하지_않으면_인증번호_검증이_실패한다() {
        // given
        EmailVerificationPurpose verificationPurpose = SIGN_UP;

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(EMAIL, verificationPurpose))
                .willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() ->
                        emailVerificationService.verify(verifyCommand(verificationPurpose, VERIFICATION_CODE))
                )
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(NOT_FOUND);

        then(saveEmailVerificationPort)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 일치하지_않는_인증번호를_입력하면_인증번호_검증이_실패한다() {
        // given
        EmailVerificationPurpose verificationPurpose = SIGN_UP;

        EmailVerification emailVerification = emailVerification(
                verificationPurpose,
                currentTimeProvider.now().minusMinutes(1)
        );

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(EMAIL, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() ->
                        emailVerificationService.verify(verifyCommand(verificationPurpose, "000000"))
                )
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(CODE_MISMATCH);

        then(saveEmailVerificationPort)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 만료된_인증번호를_입력하면_인증번호_검증이_실패한다() {
        // given
        EmailVerificationPurpose verificationPurpose = SIGN_UP;

        EmailVerification emailVerification = emailVerification(
                verificationPurpose,
                currentTimeProvider.now().minus(EXPIRE_DURATION).minusSeconds(1)
        );

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(EMAIL, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() ->
                        emailVerificationService.verify(verifyCommand(verificationPurpose, VERIFICATION_CODE))
                )
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(EXPIRED);

        then(saveEmailVerificationPort)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 이미_인증이_완료된_인증정보이면_인증번호_검증이_거부된다() {
        // given
        EmailVerificationPurpose verificationPurpose = SIGN_UP;

        EmailVerification emailVerification = emailVerification(
                verificationPurpose,
                currentTimeProvider.now().minusMinutes(2)
        );
        emailVerification.verify(VERIFICATION_CODE, currentTimeProvider.now().minusMinutes(1));

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(EMAIL, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() ->
                        emailVerificationService.verify(verifyCommand(verificationPurpose, VERIFICATION_CODE))
                )
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(ALREADY_VERIFIED);

        then(saveEmailVerificationPort)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 인증이_완료된_인증정보이면_인증_완료_확인에_성공한다() {
        // given
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        EmailVerification emailVerification = emailVerification(
                verificationPurpose,
                currentTimeProvider.now().minusMinutes(2)
        );
        emailVerification.verify(VERIFICATION_CODE, currentTimeProvider.now().minusMinutes(1));

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(EMAIL, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when
        emailVerificationService.ensureVerified(ensureVerifiedCommand(verificationPurpose));

        // then
        then(loadEmailVerificationPort)
                .should()
                .loadByEmailAndVerificationPurpose(EMAIL, verificationPurpose);
    }

    @Test
    void 인증정보가_존재하지_않으면_인증_완료_확인이_실패한다() {
        // given
        EmailVerificationPurpose verificationPurpose = SIGN_UP;

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(EMAIL, verificationPurpose))
                .willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() ->
                        emailVerificationService.ensureVerified(ensureVerifiedCommand(verificationPurpose))
                )
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(NOT_VERIFIED);
    }

    @Test
    void 인증이_완료되지_않은_인증정보이면_인증_완료_확인이_실패한다() {
        // given
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        EmailVerification emailVerification = emailVerification(
                verificationPurpose,
                currentTimeProvider.now().minusMinutes(1)
        );

        given(loadEmailVerificationPort.loadByEmailAndVerificationPurpose(EMAIL, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() ->
                        emailVerificationService.ensureVerified(ensureVerifiedCommand(verificationPurpose))
                )
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(NOT_VERIFIED);
    }

    private EmailVerificationCodeSendCommand sendCommand(EmailVerificationPurpose verificationPurpose) {
        return EmailVerificationCodeSendCommand.of(EMAIL, verificationPurpose);
    }

    private EmailVerificationCodeVerifyCommand verifyCommand(
            EmailVerificationPurpose verificationPurpose,
            String verificationCode
    ) {
        return EmailVerificationCodeVerifyCommand.of(EMAIL, verificationPurpose, verificationCode);
    }

    private EmailVerificationEnsureVerifiedCommand ensureVerifiedCommand(EmailVerificationPurpose verificationPurpose) {
        return EmailVerificationEnsureVerifiedCommand.of(EMAIL, verificationPurpose);
    }

    private EmailVerification emailVerification(
            EmailVerificationPurpose verificationPurpose,
            LocalDateTime requestedAt
    ) {
        return EmailVerification.create(EMAIL, verificationPurpose, VERIFICATION_CODE, requestedAt);
    }

}
