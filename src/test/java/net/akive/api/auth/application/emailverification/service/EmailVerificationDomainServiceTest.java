package net.akive.api.auth.application.emailverification.service;

import net.akive.api.auth.application.emailverification.required.EmailVerificationCodeGenerator;
import net.akive.api.auth.application.emailverification.required.EmailVerificationStore;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.time.CurrentTimeProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.MINUTES;
import static net.akive.api.auth.domain.emailverification.EmailVerificationFixture.VERIFICATION_CODE;
import static net.akive.api.auth.domain.emailverification.EmailVerificationFixture.createEmailVerification;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_EXPIRATION_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.EMAIL_NOT_VERIFIED;
import static net.akive.api.common.exception.business.ResourceNotFoundError.EMAIL_VERIFICATION_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.fixture.UserFixture.EMAIL;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationDomainServiceTest {

    private static final Instant NOW = Instant.parse("2025-05-21T01:00:00Z");

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
        given(currentTimeProvider.instant()).willReturn(NOW);
        given(emailVerificationCodeGenerator.generate()).willReturn(VERIFICATION_CODE);
        given(emailVerificationStore.findByEmailAndVerificationPurpose(EMAIL, SIGN_UP))
                .willReturn(Optional.empty());

        EmailVerification emailVerification = emailVerificationDomainService.issue(EMAIL, SIGN_UP);

        assertThat(emailVerification.getEmail()).isEqualTo(EMAIL);
        assertThat(emailVerification.getVerificationPurpose()).isEqualTo(SIGN_UP);
        assertThat(emailVerification.getVerificationCode()).isEqualTo(VERIFICATION_CODE);
        assertThat(emailVerification.isVerified()).isFalse();
        assertThat(emailVerification.getRenewalCount()).isZero();
        assertThat(emailVerification.getLastSentAt()).isEqualTo(NOW);
        assertThat(emailVerification.getExpiresAt()).isEqualTo(NOW.plus(VERIFICATION_CODE_EXPIRATION_DURATION));

        then(emailVerificationValidationService).should().validateIssuable(EMAIL, SIGN_UP);
    }

    @Test
    void 기존_인증정보가_존재하면_새_인증번호로_갱신한다() {
        given(currentTimeProvider.instant()).willReturn(NOW);

        EmailVerificationCode newVerificationCode = new EmailVerificationCode("654321");
        EmailVerification previous = createEmailVerification(
                VERIFICATION_CODE,
                SIGN_UP,
                NOW.minus(5, MINUTES)
        );

        given(emailVerificationCodeGenerator.generate()).willReturn(newVerificationCode);
        given(emailVerificationStore.findByEmailAndVerificationPurpose(EMAIL, SIGN_UP))
                .willReturn(Optional.of(previous));

        EmailVerification emailVerification = emailVerificationDomainService.issue(EMAIL, SIGN_UP);

        assertThat(emailVerification.getVerificationCode()).isEqualTo(newVerificationCode);
        assertThat(emailVerification.getRenewalCount()).isEqualTo(1);
        assertThat(emailVerification.isVerified()).isFalse();
        assertThat(emailVerification.getLastSentAt()).isEqualTo(NOW);
        assertThat(emailVerification.getExpiresAt()).isEqualTo(NOW.plus(VERIFICATION_CODE_EXPIRATION_DURATION));
    }

    @Test
    void 인증번호_발급이_허용되지_않으면_후속_처리를_하지_않는다() {
        ResourceNotFoundException exception = new ResourceNotFoundException(USER_NOT_FOUND);

        willThrow(exception).given(emailVerificationValidationService).validateIssuable(EMAIL, SIGN_UP);

        assertThatThrownBy(() -> emailVerificationDomainService.issue(EMAIL, SIGN_UP)).isSameAs(exception);

        then(currentTimeProvider).should(never()).instant();
        then(emailVerificationCodeGenerator).should(never()).generate();
        then(emailVerificationStore).should(never()).findByEmailAndVerificationPurpose(any(), any());
    }

    @Test
    void 이메일_인증을_완료한다() {
        EmailVerification emailVerification = createEmailVerification(
                VERIFICATION_CODE,
                SIGN_UP,
                NOW.minus(1, MINUTES)
        );

        given(currentTimeProvider.instant()).willReturn(NOW);
        given(emailVerificationStore.findByEmailAndVerificationPurpose(EMAIL, SIGN_UP))
                .willReturn(Optional.of(emailVerification));

        EmailVerification verifiedEmailVerification = emailVerificationDomainService.verify(
                EMAIL,
                SIGN_UP,
                VERIFICATION_CODE
        );

        assertThat(verifiedEmailVerification.isVerified()).isTrue();
    }

    @Test
    void 인증정보가_존재하지_않으면_인증번호_검증이_실패한다() {
        given(emailVerificationStore.findByEmailAndVerificationPurpose(EMAIL, SIGN_UP))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> emailVerificationDomainService.verify(EMAIL, SIGN_UP, VERIFICATION_CODE))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(EMAIL_VERIFICATION_NOT_FOUND);
    }

    @Test
    void 인증_완료_확인한다() {
        EmailVerification emailVerification = createEmailVerification(VERIFICATION_CODE, SIGN_UP, NOW);
        emailVerification.verify(VERIFICATION_CODE, NOW.plus(1, MINUTES));

        given(emailVerificationStore.findByEmailAndVerificationPurpose(EMAIL, SIGN_UP))
                .willReturn(Optional.of(emailVerification));

        emailVerificationDomainService.checkVerified(EMAIL, SIGN_UP);

        then(emailVerificationStore).should().findByEmailAndVerificationPurpose(EMAIL, SIGN_UP);
    }

    @Test
    void 인증정보가_존재하지_않으면_인증_완료_확인에_실패한다() {
        given(emailVerificationStore.findByEmailAndVerificationPurpose(EMAIL, SIGN_UP))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> emailVerificationDomainService.checkVerified(EMAIL, SIGN_UP))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(EMAIL_NOT_VERIFIED);
    }

    @Test
    void 인증이_완료되지_않은_인증정보이면_인증_완료_확인에_실패한다() {
        EmailVerification emailVerification = createEmailVerification(
                VERIFICATION_CODE,
                SIGN_UP,
                NOW.minus(1, MINUTES)
        );

        given(emailVerificationStore.findByEmailAndVerificationPurpose(EMAIL, SIGN_UP))
                .willReturn(Optional.of(emailVerification));

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> emailVerificationDomainService.checkVerified(EMAIL, SIGN_UP))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(EMAIL_NOT_VERIFIED);
    }
}
