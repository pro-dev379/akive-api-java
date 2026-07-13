package net.akive.api.user.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.result.token.IssueTokenResult;
import net.akive.api.auth.application.port.in.token.IssueTokenUseCase;
import net.akive.api.mentoring.application.port.in.CreateDefaultMentoringProfileUseCase;
import net.akive.api.user.application.dto.SignUpResult;
import net.akive.api.user.application.dto.SignUpByEmailCommand;
import net.akive.api.user.application.port.in.SignUpByEmailUseCase;
import net.akive.api.user.application.port.out.SaveGoalPort;
import net.akive.api.user.application.port.out.SaveUserPort;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class SignUpService implements SignUpByEmailUseCase {

    private final SaveUserPort saveUserPort;
    private final SaveGoalPort saveGoalPort;

    private final SignUpDomainService signUpDomainService;
    private final CreateDefaultMentoringProfileUseCase createDefaultMentoringProfileUseCase;
    private final IssueTokenUseCase issueTokenUseCase;

    @Override
    @Transactional
    public SignUpResult signUp(SignUpByEmailCommand command) {
        User savedUser = saveUserPort.save(
                signUpDomainService.signUpByEmail(
                        command.email(),
                        command.password(),
                        command.roleType()
                )
        );
        Long userId = savedUser.getUserId();
        IssueTokenResult issuedToken = issueTokenUseCase.issue(userId, savedUser.getRoleType());

        saveGoalPort.save(Goal.createDefault(userId));
        createDefaultMentoringProfileUseCase.create(userId);

        return new SignUpResult(savedUser.getNickname(), issuedToken.accessToken(), issuedToken.refreshToken());
    }
}
