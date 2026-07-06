package net.akive.api.user.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.command.emailverification.EnsureEmailVerifiedCommand;
import net.akive.api.auth.application.dto.result.token.IssueTokenResult;
import net.akive.api.auth.application.port.in.emailverification.EnsureEmailVerifiedUseCase;
import net.akive.api.auth.application.port.in.token.IssueTokenUseCase;
import net.akive.api.mentoring.application.port.in.CreateDefaultMentoringProfileUseCase;
import net.akive.api.user.application.dto.SignUpResult;
import net.akive.api.user.application.dto.SignUpByEmailCommand;
import net.akive.api.user.application.port.in.SignUpByEmailUseCase;
import net.akive.api.user.application.port.out.GenerateNicknamePort;
import net.akive.api.user.application.port.out.SaveGoalPort;
import net.akive.api.user.application.port.out.SaveUserPort;
import net.akive.api.user.application.port.out.ValidateUserPort;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import net.akive.api.user.exception.UserException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.user.exception.UserErrorCode.EMAIL_ALREADY_EXISTS;

@Service
@RequiredArgsConstructor
class SignUpService implements SignUpByEmailUseCase {

    private final SaveUserPort saveUserPort;
    private final SaveGoalPort saveGoalPort;
    private final ValidateUserPort validateUserPort;
    private final GenerateNicknamePort generateNicknamePort;

    private final EnsureEmailVerifiedUseCase ensureEmailVerifiedUseCase;
    private final CreateDefaultMentoringProfileUseCase createDefaultMentoringProfileUseCase;
    private final IssueTokenUseCase issueTokenUseCase;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SignUpResult signUp(SignUpByEmailCommand command) {
        if (validateUserPort.existsByEmail(command.email())) {
            throw new UserException(EMAIL_ALREADY_EXISTS);
        }

        ensureEmailVerifiedUseCase.ensureVerified(
                EnsureEmailVerifiedCommand.of(
                        command.email(),
                        SIGN_UP
                )
        );

        String nickname = generateNicknamePort.generate(command.roleType());
        String encodedPassword = passwordEncoder.encode(command.password());

        User savedUser = saveUserPort.save(
                User.signUpByEmail(
                        command.email(),
                        nickname,
                        encodedPassword,
                        command.roleType()
                )
        );
        Long userId = savedUser.getUserId();
        IssueTokenResult issuedToken = issueTokenUseCase.issue(userId, savedUser.getRoleType());

        saveGoalPort.save(Goal.createDefault(userId));
        createDefaultMentoringProfileUseCase.create(userId);

        return new SignUpResult(nickname, issuedToken.accessToken(), issuedToken.refreshToken());
    }
}
