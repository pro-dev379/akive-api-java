package net.akive.api.user.application.service;

import net.akive.api.common.domain.Email;
import net.akive.api.auth.application.dto.result.token.IssueTokenResult;
import net.akive.api.auth.application.port.in.token.IssueTokenUseCase;
import net.akive.api.auth.domain.emailverification.exception.EmailVerificationException;
import net.akive.api.mentoring.application.port.in.CreateDefaultMentoringProfileUseCase;
import net.akive.api.user.application.dto.SignUpByEmailCommand;
import net.akive.api.user.application.dto.SignUpResult;
import net.akive.api.user.application.port.out.SaveGoalPort;
import net.akive.api.user.application.port.out.SaveUserPort;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.auth.domain.emailverification.exception.EmailVerificationErrorCode.NOT_VERIFIED;
import static net.akive.api.user.domain.LoginType.EMAIL;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

    @Mock
    SaveUserPort saveUserPort;

    @Mock
    SaveGoalPort saveGoalPort;

    @Mock
    SignUpDomainService signUpDomainService;

    @Mock
    CreateDefaultMentoringProfileUseCase createDefaultMentoringProfileUseCase;

    @Mock
    IssueTokenUseCase issueTokenUseCase;

    @InjectMocks
    SignUpService signUpService;

    @Test
    void 이메일로_회원가입하면_회원_정보와_기본_데이터를_생성하고_토큰을_발급한다() {
        // given
        SignUpByEmailCommand command = new SignUpByEmailCommand(
                Email.of("test@test.com"),
                USER,
                "password123!"
        );

        String nickname = "USER@abcde";
        String encodedPassword = "encodedPassword";

        User user = User.signUpByEmail(
                command.email(),
                nickname,
                encodedPassword,
                USER
        );
        User savedUser = User.create(
                1L,
                user.getEmail(),
                user.getRoleType(),
                EMAIL,
                user.getNickname(),
                user.getPassword(),
                null,
                null,
                false,
                null
        );

        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        IssueTokenResult issuedToken = new IssueTokenResult(accessToken, refreshToken);

        when(signUpDomainService.signUpByEmail(command.email(), command.password(), command.roleType()))
                .thenReturn(user);
        when(saveUserPort.save(any(User.class))).thenReturn(savedUser);
        when(issueTokenUseCase.issue(savedUser.getUserId(), USER)).thenReturn(issuedToken);

        // when
        SignUpResult result = signUpService.signUp(command);

        // then
        assertThat(result.nickname()).isEqualTo(nickname);
        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);

        then(signUpDomainService)
                .should()
                .signUpByEmail(command.email(), command.password(), command.roleType());
        then(saveUserPort).should().save(user);

        then(issueTokenUseCase).should().issue(savedUser.getUserId(), USER);

        then(saveGoalPort).should()
                .save(argThat(goal ->
                        goal.getUserId().equals(savedUser.getUserId())
                ));

        then(createDefaultMentoringProfileUseCase).should().create(savedUser.getUserId());
    }

    @Test
    void 회원가입할_수_없으면_회원_정보와_기본_데이터를_생성하지_않고_토큰을_발급하지_않는다() {
        // given
        SignUpByEmailCommand command = new SignUpByEmailCommand(
                Email.of("test@test.com"),
                USER,
                "password123!"
        );

        doThrow(new EmailVerificationException(NOT_VERIFIED))
                .when(signUpDomainService)
                .signUpByEmail(command.email(), command.password(), command.roleType());

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() -> signUpService.signUp(command));

        then(saveUserPort).should(never()).save(any(User.class));
        then(issueTokenUseCase).should(never()).issue(any(), any());
        then(saveGoalPort).should(never()).save(any(Goal.class));
        then(createDefaultMentoringProfileUseCase).should(never()).create(any());
    }
}
