package net.akive.api.auth.application.password.service;

import net.akive.api.auth.application.emailverification.provided.EmailVerificationChecker;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationEnsureCommand;
import net.akive.api.auth.application.password.provided.dto.PasswordResetCommand;
import net.akive.api.auth.application.password.provided.dto.PasswordUpdateCommand;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.PASSWORD_RESET;
import static net.akive.api.auth.application.password.fixture.PasswordCommandFixture.*;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_EMAIL_OR_PASSWORD;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.EMAIL_NOT_VERIFIED;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PasswordCommandServiceTest {

    @Mock
    UserFinder userFinder;

    @Mock
    UserAccountManager userAccountManager;

    @Mock
    EmailVerificationChecker emailVerificationChecker;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    PasswordCommandService passwordCommandService;

    @Test
    void 패스워드를_변경한다() {
        PasswordUpdateCommand command = createPasswordUpdateCommand();
        User user = createUser(USER_ID, PASSWORD_HASH);

        given(userFinder.findByUserIdAndNotWithdrawn(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(command.currentPassword(), user.getPasswordHash())).willReturn(true);
        given(passwordEncoder.encode(command.targetPassword())).willReturn(TARGET_PASSWORD_HASH);

        passwordCommandService.update(command);

        then(userFinder).should().findByUserIdAndNotWithdrawn(USER_ID);
        then(passwordEncoder).should().matches(command.currentPassword(), user.getPasswordHash());
        then(passwordEncoder).should().encode(command.targetPassword());
        then(userAccountManager).should().updatePasswordByUserId(USER_ID, TARGET_PASSWORD_HASH);
    }

    @Test
    void 회원을_조회할_수_없으면_패스워드_변경에_실패한다() {
        PasswordUpdateCommand command = createPasswordUpdateCommand(UNKNOWN_USER_ID);

        given(userFinder.findByUserIdAndNotWithdrawn(UNKNOWN_USER_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> passwordCommandService.update(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(passwordEncoder).shouldHaveNoInteractions();
        then(userAccountManager).shouldHaveNoInteractions();
    }

    @Test
    void 회원이_입력한_현재_패스워드와_기존_패스워드가_일치하지_않으면_패스워드_변경에_실패한다() {
        PasswordUpdateCommand command = createPasswordUpdateCommand();
        User user = createUser(USER_ID, PASSWORD_HASH);

        given(userFinder.findByUserIdAndNotWithdrawn(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(command.currentPassword(), user.getPasswordHash())).willReturn(false);

        assertThatExceptionOfType(AuthenticationException.class)
                .isThrownBy(() -> passwordCommandService.update(command))
                .extracting(AuthenticationException::getError)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(passwordEncoder).should().matches(command.currentPassword(), user.getPasswordHash());
        then(passwordEncoder).should(never()).encode(command.targetPassword());
        then(userAccountManager).shouldHaveNoInteractions();
    }

    @Test
    void 패스워드를_리셋한다() {
        PasswordResetCommand command = createPasswordResetCommand();
        EmailVerificationEnsureCommand emailVerificationEnsureCommand =
                new EmailVerificationEnsureCommand(command.email(), PASSWORD_RESET);

        given(passwordEncoder.encode(command.targetPassword())).willReturn(TARGET_PASSWORD_HASH);

        passwordCommandService.reset(command);

        then(emailVerificationChecker).should().ensureEmailVerified(emailVerificationEnsureCommand);
        then(passwordEncoder).should().encode(command.targetPassword());
        then(userAccountManager).should().updatePasswordByEmail(command.email(), TARGET_PASSWORD_HASH);
    }

    @Test
    void 이메일이_인증되지_않으면_패스워드_리셋에_실패한다() {
        PasswordResetCommand command = createPasswordResetCommand();
        EmailVerificationEnsureCommand emailVerificationEnsureCommand =
                new EmailVerificationEnsureCommand(command.email(), PASSWORD_RESET);
        willThrow(new BusinessRuleViolationException(EMAIL_NOT_VERIFIED))
                .given(emailVerificationChecker)
                .ensureEmailVerified(emailVerificationEnsureCommand);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> passwordCommandService.reset(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(EMAIL_NOT_VERIFIED);

        then(passwordEncoder).shouldHaveNoInteractions();
        then(userAccountManager).shouldHaveNoInteractions();
    }

    @Test
    void 이메일에_해당하는_회원을_조회할_수_없으면_패스워드_리셋에_실패한다() {
        PasswordResetCommand command = createPasswordResetCommand();
        EmailVerificationEnsureCommand emailVerificationEnsureCommand =
                new EmailVerificationEnsureCommand(command.email(), PASSWORD_RESET);

        given(passwordEncoder.encode(command.targetPassword())).willReturn(TARGET_PASSWORD_HASH);
        willThrow(new ResourceNotFoundException(USER_NOT_FOUND))
                .given(userAccountManager)
                .updatePasswordByEmail(command.email(), TARGET_PASSWORD_HASH);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> passwordCommandService.reset(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(emailVerificationChecker).should().ensureEmailVerified(emailVerificationEnsureCommand);
        then(passwordEncoder).should().encode(command.targetPassword());
        then(userAccountManager).should().updatePasswordByEmail(command.email(), TARGET_PASSWORD_HASH);
    }
}
