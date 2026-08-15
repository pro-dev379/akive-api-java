package net.akive.api.auth.application.emailverification.service;

import net.akive.api.auth.application.emailverification.required.EmailVerificationCodeGenerator;
import net.akive.api.auth.application.emailverification.required.EmailVerificationStore;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.common.time.CurrentTimeProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static net.akive.api.auth.domain.emailverification.EmailVerificationFixture.VERIFICATION_CODE;
import static net.akive.api.auth.domain.emailverification.EmailVerificationFixture.createEmailVerification;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_EXPIRATION_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.common.domain.EmailFixture.createEmail;
import static net.akive.api.common.exception.business.BadRequestError.EMAIL_NOT_VERIFIED;
import static net.akive.api.common.exception.business.NotFoundError.EMAIL_VERIFICATION_NOT_FOUND;
import static net.akive.api.common.exception.business.NotFoundError.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationDomainServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2025, 5, 21, 10, 0);

    @Mock
    EmailVerificationValidationService emailVerificationValidationService;

    @Mock
    EmailVerificationCodeGenerator emailVerificationCodeGenerator;

    @Mock
    EmailVerificationStore emailVerificationStore;

    @Mock
    CurrentTimeProvider currentTimeProvider;

    @InjectMocks
    EmailVerificationDomainService emailVerificationDomainService;

    @Test
    void 새로운_이메일_인증번호를_발급한다() {
        Email email = createEmail();

        given(currentTimeProvider.now()).willReturn(NOW);
        given(emailVerificationCodeGenerator.generate()).willReturn(VERIFICATION_CODE);
        given(emailVerificationStore.findByEmailAndVerificationPurpose(email, SIGN_UP))
                .willReturn(Optional.empty());

        EmailVerification emailVerification = emailVerificationDomainService.issue(email, SIGN_UP);

        assertThat(emailVerification.getEmail()).isEqualTo(email);
        assertThat(emailVerification.getVerificationPurpose()).isEqualTo(SIGN_UP);
        assertThat(emailVerification.getVerificationCode()).isEqualTo(VERIFICATION_CODE);
        assertThat(emailVerification.isVerified()).isFalse();
        assertThat(emailVerification.getRenewalCount()).isZero();
        assertThat(emailVerification.getLastSentAt()).isEqualTo(NOW);
        assertThat(emailVerification.getExpiresAt()).isEqualTo(NOW.plus(VERIFICATION_CODE_EXPIRATION_DURATION));

        then(emailVerificationValidationService).should().validateIssuable(email, SIGN_UP);
    }

    @Test
    void 기존_인증정보가_존재하면_새_인증번호로_갱신한다() {
        Email email = createEmail();
        given(currentTimeProvider.now()).willReturn(NOW);

        EmailVerification previous = createEmailVerification(
                SIGN_UP,
                LocalDateTime.of(2025, 5, 21, 9, 55)
        );

        given(emailVerificationCodeGenerator.generate()).willReturn(VERIFICATION_CODE);
        given(emailVerificationStore.findByEmailAndVerificationPurpose(email, SIGN_UP))
                .willReturn(Optional.of(previous));

        EmailVerification emailVerification = emailVerificationDomainService.issue(email, SIGN_UP);

        assertThat(emailVerification.getVerificationCode()).isEqualTo(VERIFICATION_CODE);
        assertThat(emailVerification.getRenewalCount()).isEqualTo(1);
        assertThat(emailVerification.isVerified()).isFalse();
        assertThat(emailVerification.getLastSentAt()).isEqualTo(NOW);
        assertThat(emailVerification.getExpiresAt()).isEqualTo(NOW.plus(VERIFICATION_CODE_EXPIRATION_DURATION));
    }

    @Test
    void 인증번호_발급이_허용되지_않으면_후속_처리를_하지_않는다() {
        Email email = createEmail();
        NotFoundException exception = new NotFoundException(USER_NOT_FOUND);

        willThrow(exception).given(emailVerificationValidationService).validateIssuable(email, SIGN_UP);

        assertThatThrownBy(() -> emailVerificationDomainService.issue(email, SIGN_UP)).isSameAs(exception);

        then(currentTimeProvider).should(never()).now();
        then(emailVerificationCodeGenerator).should(never()).generate();
        then(emailVerificationStore).should(never()).findByEmailAndVerificationPurpose(any(), any());
    }

    @Test
    void 이메일_인증을_완료한다() {
        Email email = createEmail();
        EmailVerification emailVerification = createEmailVerification(SIGN_UP, NOW.minusMinutes(1));

        given(currentTimeProvider.now()).willReturn(NOW);
        given(emailVerificationStore.findByEmailAndVerificationPurpose(email, SIGN_UP))
                .willReturn(Optional.of(emailVerification));

        EmailVerification verifiedEmailVerification = emailVerificationDomainService.verify(
                email,
                SIGN_UP,
                VERIFICATION_CODE
        );

        assertThat(verifiedEmailVerification.isVerified()).isTrue();
    }

    @Test
    void 인증정보가_존재하지_않으면_인증번호_검증이_실패한다() {
        Email email = createEmail();

        given(emailVerificationStore.findByEmailAndVerificationPurpose(email, SIGN_UP))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> emailVerificationDomainService.verify(email, SIGN_UP, VERIFICATION_CODE))
                .extracting(NotFoundException::getError)
                .isEqualTo(EMAIL_VERIFICATION_NOT_FOUND);
    }

    @Test
    void 인증_완료_확인한다() {
        Email email = createEmail();
        EmailVerification emailVerification = createEmailVerification(SIGN_UP, NOW.minusMinutes(2));
        emailVerification.verify(VERIFICATION_CODE, NOW.minusMinutes(1));

        given(emailVerificationStore.findByEmailAndVerificationPurpose(email, SIGN_UP))
                .willReturn(Optional.of(emailVerification));

        emailVerificationDomainService.checkVerified(email, SIGN_UP);

        then(emailVerificationStore).should().findByEmailAndVerificationPurpose(email, SIGN_UP);
    }

    @Test
    void 인증정보가_존재하지_않으면_인증_완료_확인에_실패한다() {
        Email email = createEmail();

        given(emailVerificationStore.findByEmailAndVerificationPurpose(email, SIGN_UP))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> emailVerificationDomainService.checkVerified(email, SIGN_UP))
                .extracting(BadRequestException::getError)
                .isEqualTo(EMAIL_NOT_VERIFIED);
    }

    @Test
    void 인증이_완료되지_않은_인증정보이면_인증_완료_확인에_실패한다() {
        Email email = createEmail();
        EmailVerification emailVerification = createEmailVerification(SIGN_UP, NOW.minusMinutes(1));

        given(emailVerificationStore.findByEmailAndVerificationPurpose(email, SIGN_UP))
                .willReturn(Optional.of(emailVerification));

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> emailVerificationDomainService.checkVerified(email, SIGN_UP))
                .extracting(BadRequestException::getError)
                .isEqualTo(EMAIL_NOT_VERIFIED);
    }
}
