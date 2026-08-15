package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.token.provided.TokenIssuer;
import net.akive.api.auth.application.token.provided.dto.TokenIssueCommand;
import net.akive.api.auth.domain.token.IssuedToken;
import net.akive.api.mentoring.application.provided.MentoringProfileCreator;
import net.akive.api.user.application.provided.SignUpByEmailUseCase;
import net.akive.api.user.application.provided.dto.SignUpByEmailCommand;
import net.akive.api.user.application.provided.dto.SignUpResult;
import net.akive.api.user.application.required.SaveGoalPort;
import net.akive.api.user.application.required.SaveUserPort;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
class SignUpService implements SignUpByEmailUseCase {

    private final SaveUserPort saveUserPort;
    private final SaveGoalPort saveGoalPort;

    private final SignUpDomainService signUpDomainService;
    private final MentoringProfileCreator mentoringProfileCreator;
    private final TokenIssuer tokenIssuer;

    @Override
    public SignUpResult signUp(SignUpByEmailCommand command) {
        User user = signUpDomainService.signUpByEmail(
                command.email(),
                command.password(),
                command.roleType()
        );

        User savedUser = saveUserPort.save(user);

        Long userId = savedUser.getUserId();
        TokenIssueCommand tokenIssueCommand = new TokenIssueCommand(
                userId,
                command.clientInstanceId(),
                command.roleType()
        );


        IssuedToken token = tokenIssuer.issue(tokenIssueCommand);

        saveGoalPort.save(Goal.createDefault(userId));

        mentoringProfileCreator.create(userId);

        return new SignUpResult(
                savedUser.getNickname(),
                token.accessToken().value(),
                token.refreshToken().value()
        );
    }
}
