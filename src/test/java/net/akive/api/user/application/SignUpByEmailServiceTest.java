package net.akive.api.user.application;

import net.akive.api.auth.application.dto.command.emailverification.EmailVerificationEnsureVerifiedCommand;
import net.akive.api.auth.application.dto.result.TokenIssueResult;
import net.akive.api.auth.application.port.in.emailverification.EnsureEmailVerifiedUseCase;
import net.akive.api.auth.application.port.in.token.IssueTokenUseCase;
import net.akive.api.auth.domain.emailverification.exception.EmailVerificationException;
import net.akive.api.mentoring.application.port.in.CreateDefaultMentoringProfileUseCase;
import net.akive.api.user.application.dto.SignUpByEmailCommand;
import net.akive.api.user.application.dto.SignUpResult;
import net.akive.api.user.application.port.out.GenerateNicknamePort;
import net.akive.api.user.application.port.out.SaveGoalPort;
import net.akive.api.user.application.port.out.SaveUserPort;
import net.akive.api.user.application.port.out.ValidateUserPort;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import net.akive.api.user.excpetion.UserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.auth.domain.emailverification.exception.EmailVerificationErrorCode.NOT_VERIFIED;
import static net.akive.api.user.domain.RoleType.USER;
import static net.akive.api.user.excpetion.UserErrorCode.EMAIL_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignUpByEmailServiceTest {

    @Mock
    SaveUserPort saveUserPort;

    @Mock
    SaveGoalPort saveGoalPort;
    
    @Mock
    ValidateUserPort validateUserPort;

    @Mock
    GenerateNicknamePort generateNicknamePort;

    @Mock
    EnsureEmailVerifiedUseCase ensureEmailVerifiedUseCase;

    @Mock
    CreateDefaultMentoringProfileUseCase createDefaultMentoringProfileUseCase;

    @Mock
    IssueTokenUseCase issueTokenUseCase;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    SignUpByEmailService signUpByEmailService;

    @Test
    void 이메일로_회원가입하면_회원_정보와_기본_데이터를_생성하고_토큰을_발급한다() {
        // given
        SignUpByEmailCommand command = new SignUpByEmailCommand(
                "test@test.com",
                USER,
                "password123!"
        );

        String nickname = "USER@abcde";
        String encodedPassword = "encodedPassword";

        User savedUser = User.builder()
                .userId(1L)
                .email(command.email())
                .nickname(nickname)
                .password(encodedPassword)
                .roleType(USER)
                .build();

        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        TokenIssueResult issuedToken = new TokenIssueResult(accessToken, refreshToken);

        when(generateNicknamePort.generate(USER)).thenReturn(nickname);
        when(passwordEncoder.encode(command.password())).thenReturn(encodedPassword);
        when(saveUserPort.save(any(User.class))).thenReturn(savedUser);
        when(issueTokenUseCase.issue(savedUser.getUserId(), USER)).thenReturn(issuedToken);

        // when
        SignUpResult result = signUpByEmailService.signUp(command);

        // then
        assertThat(result.nickname()).isEqualTo(nickname);
        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);

        then(ensureEmailVerifiedUseCase)
                .should()
                .ensureVerified(
                        EmailVerificationEnsureVerifiedCommand.of(command.email(), SIGN_UP)
                );

        then(generateNicknamePort).should().generate(USER);
        then(passwordEncoder).should().encode(command.password());

        then(saveUserPort).should()
                .save(argThat(user ->
                        user.getEmail().equals(command.email())
                                && user.getNickname().equals(nickname)
                                && user.getPassword().equals(encodedPassword)
                                && user.getRoleType() == USER
                ));

        then(issueTokenUseCase).should().issue(savedUser.getUserId(), USER);

        then(saveGoalPort).should().save(argThat(goal ->
                goal.getUserId().equals(savedUser.getUserId())
        ));

        then(createDefaultMentoringProfileUseCase).should().create(savedUser.getUserId());
    }

    @Test
    void 이메일_인증이_되지_않으면_회원가입에_실패한다() {
        // given
        SignUpByEmailCommand command = new SignUpByEmailCommand(
                "test@test.com",
                USER,
                "password123!"
        );

        doThrow(new EmailVerificationException(NOT_VERIFIED))
                .when(ensureEmailVerifiedUseCase)
                .ensureVerified(EmailVerificationEnsureVerifiedCommand.of(command.email(), SIGN_UP));

        // when & then
        assertThatThrownBy(() -> signUpByEmailService.signUp(command))
                .isInstanceOf(EmailVerificationException.class)
                .satisfies(e ->
                        assertThat(((EmailVerificationException) e).getErrorCode())
                                .isEqualTo(NOT_VERIFIED)
                );

        then(generateNicknamePort).should(never()).generate(command.roleType());
        then(passwordEncoder).should(never()).encode(command.password());
        then(saveUserPort).should(never()).save(any(User.class));
        then(issueTokenUseCase).should(never()).issue(any(), any());
        then(saveGoalPort).should(never()).save(any(Goal.class));
        then(createDefaultMentoringProfileUseCase).should(never()).create(any());
    }

    @Test
    void 이미_가입된_이메일이면_회원가입에_실패한다() {
        // given
        SignUpByEmailCommand command = new SignUpByEmailCommand(
                "test@test.com",
                USER,
                "password123!"
        );

        when(validateUserPort.existsByEmail(command.email()))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> signUpByEmailService.signUp(command))
                .isInstanceOf(UserException.class)
                .satisfies(e ->
                        assertThat(((UserException) e).getErrorCode())
                                .isEqualTo(EMAIL_ALREADY_EXISTS)
                );

        then(ensureEmailVerifiedUseCase).should(never()).ensureVerified(any());
        then(generateNicknamePort).should(never()).generate(any());
        then(passwordEncoder).should(never()).encode(any());
        then(saveUserPort).should(never()).save(any());
        then(issueTokenUseCase).should(never()).issue(any(), any());
        then(saveGoalPort).should(never()).save(any());
        then(createDefaultMentoringProfileUseCase).should(never()).create(any());
    }
}