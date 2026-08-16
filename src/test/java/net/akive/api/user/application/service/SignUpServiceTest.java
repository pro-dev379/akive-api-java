package net.akive.api.user.application.service;

import net.akive.api.auth.application.token.provided.TokenIssuer;
import net.akive.api.auth.application.token.provided.dto.TokenIssueCommand;
import net.akive.api.auth.domain.token.IssuedToken;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.mentoring.application.provided.MentoringProfileCreator;
import net.akive.api.user.application.provided.dto.SignUpByEmailCommand;
import net.akive.api.user.application.provided.dto.SignUpResult;
import net.akive.api.user.application.required.SaveGoalPort;
import net.akive.api.user.application.required.SaveUserPort;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.auth.application.token.provided.dto.TokenIssueCommandFixture.createTokenIssueCommand;
import static net.akive.api.auth.domain.token.IssuedTokenFixture.createIssuedToken;
import static net.akive.api.common.domain.ClientInstanceIdFixture.createClientInstanceId;
import static net.akive.api.common.exception.business.BadRequestError.EMAIL_NOT_VERIFIED;
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
    MentoringProfileCreator mentoringProfileCreator;

    @Mock
    TokenIssuer tokenIssuer;

    @InjectMocks
    SignUpService signUpService;

    @Test
    void 이메일로_회원가입하면_회원_정보와_기본_데이터를_생성하고_토큰을_발급한다() {
        // given
        ClientInstanceId clientInstanceId = createClientInstanceId();
        SignUpByEmailCommand command = new SignUpByEmailCommand(
                clientInstanceId,
                new Email("test@test.com"),
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
        User savedUser = User.reconstruct(
                1L,
                user.getEmail(),
                user.getRoleType(),
                EMAIL,
                user.getNickname(),
                user.getPasswordHash(),
                null,
                null,
                false,
                null
        );

        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        IssuedToken issuedToken = createIssuedToken(accessToken, refreshToken);

        when(signUpDomainService.signUpByEmail(command.email(), command.password(), command.roleType()))
                .thenReturn(user);
        when(saveUserPort.save(any(User.class))).thenReturn(savedUser);
        when(tokenIssuer.issue(createTokenIssueCommand(savedUser.getUserId(), clientInstanceId, USER)))
                .thenReturn(issuedToken);

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

        then(tokenIssuer).should().issue(createTokenIssueCommand(savedUser.getUserId(), clientInstanceId, USER));

        then(saveGoalPort).should()
                .save(argThat(goal ->
                        goal.getUserId().equals(savedUser.getUserId())
                ));

        then(mentoringProfileCreator).should().create(savedUser.getUserId());
    }

    @Test
    void 회원가입할_수_없으면_회원_정보와_기본_데이터를_생성하지_않고_토큰을_발급하지_않는다() {
        // given
        SignUpByEmailCommand command = new SignUpByEmailCommand(
                createClientInstanceId(),
                new Email("test@test.com"),
                USER,
                "password123!"
        );

        doThrow(new BadRequestException(EMAIL_NOT_VERIFIED))
                .when(signUpDomainService)
                .signUpByEmail(command.email(), command.password(), command.roleType());

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> signUpService.signUp(command));

        then(saveUserPort).should(never()).save(any(User.class));
        then(tokenIssuer).should(never()).issue(any(TokenIssueCommand.class));
        then(saveGoalPort).should(never()).save(any(Goal.class));
        then(mentoringProfileCreator).should(never()).create(any());
    }
}
