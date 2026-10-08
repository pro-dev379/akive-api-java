package net.akive.api.user.application.service;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.user.application.provided.UserValidator;
import net.akive.api.user.application.required.NicknameGenerator;
import net.akive.api.user.application.required.UserEmailVerificationChecker;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.EMAIL_NOT_VERIFIED;
import static net.akive.api.common.exception.business.ResourceConflictError.EMAIL_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceConflictError.NICKNAME_ALREADY_EXISTS;
import static net.akive.api.user.domain.UserRole.MEMBER;
import static net.akive.api.user.domain.fixture.UserDetailFixture.NICKNAME;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserSignUpDomainServiceTest {

    @Mock
    UserValidator userValidator;

    @Mock
    NicknameGenerator nicknameGenerator;

    @Mock
    UserEmailVerificationChecker emailVerificationChecker;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserSignUpDomainService userSignUpDomainService;

    @Test
    void 회원_입력_닉네임으로_이메일_회원을_가입시킨다() {
        given(passwordEncoder.encode(PASSWORD)).willReturn(PASSWORD_HASH);

        User user = userSignUpDomainService.signUpMemberByEmail(EMAIL, PASSWORD, NICKNAME);

        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getRole()).isEqualTo(MEMBER);
        assertThat(user.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(user.getUserDetail().nickname()).isEqualTo(NICKNAME);

        then(userValidator).should().validateEmailNotDuplicate(EMAIL);
        then(emailVerificationChecker).should().ensureEmailVerifiedForSignUp(EMAIL);
        then(userValidator).should().validateNicknameNotDuplicate(NICKNAME);
        then(nicknameGenerator).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void 닉네임이_없으면_닉네임을_자동_생성해서_이메일_회원을_가입시킨다(String nickname) {
        given(nicknameGenerator.generate(MEMBER)).willReturn(NICKNAME);
        given(passwordEncoder.encode(PASSWORD)).willReturn(PASSWORD_HASH);

        User user = userSignUpDomainService.signUpMemberByEmail(EMAIL, PASSWORD, nickname);

        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getRole()).isEqualTo(MEMBER);
        assertThat(user.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(user.getUserDetail().nickname()).isEqualTo(NICKNAME);

        then(userValidator).should().validateEmailNotDuplicate(EMAIL);
        then(emailVerificationChecker).should().ensureEmailVerifiedForSignUp(EMAIL);
        then(userValidator).should(never()).validateNicknameNotDuplicate(NICKNAME);
        then(nicknameGenerator).should().generate(MEMBER);
    }

    @Test
    void 이메일이_중복되면_회원_가입에_실패한다() {
        willThrow(new ResourceConflictException(EMAIL_ALREADY_EXISTS))
                .given(userValidator)
                .validateEmailNotDuplicate(EMAIL);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> userSignUpDomainService.signUpMemberByEmail(EMAIL, PASSWORD, NICKNAME))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(EMAIL_ALREADY_EXISTS);

        then(emailVerificationChecker).shouldHaveNoInteractions();
        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    void 이메일_인증이_완료되지_않으면_회원_가입에_실패한다() {
        willThrow(new BusinessRuleViolationException(EMAIL_NOT_VERIFIED))
                .given(emailVerificationChecker)
                .ensureEmailVerifiedForSignUp(EMAIL);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> userSignUpDomainService.signUpMemberByEmail(EMAIL, PASSWORD, NICKNAME))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(EMAIL_NOT_VERIFIED);

        then(userValidator).should().validateEmailNotDuplicate(EMAIL);
        then(userValidator).should(never()).validateNicknameNotDuplicate(NICKNAME);
        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    void 회원_입력_닉네임이_중복되면_회원_가입에_실패한다() {
        willThrow(new ResourceConflictException(NICKNAME_ALREADY_EXISTS))
                .given(userValidator)
                .validateNicknameNotDuplicate(NICKNAME);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> userSignUpDomainService.signUpMemberByEmail(EMAIL, PASSWORD, NICKNAME))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(NICKNAME_ALREADY_EXISTS);

        then(userValidator).should().validateEmailNotDuplicate(EMAIL);
        then(emailVerificationChecker).should().ensureEmailVerifiedForSignUp(EMAIL);
        then(passwordEncoder).shouldHaveNoInteractions();
    }
}
