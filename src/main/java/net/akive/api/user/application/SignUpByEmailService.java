package net.akive.api.user.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.TokenIssueResult;
import net.akive.api.auth.application.port.in.emailverification.ValidateEmailVerificationUseCase;
import net.akive.api.auth.application.port.in.token.IssueTokenUseCase;
import net.akive.api.mentoring.application.port.in.CreateDefaultMentoringProfileUseCase;
import net.akive.api.user.application.dto.SignUpResult;
import net.akive.api.user.application.dto.SignUpByEmailCommand;
import net.akive.api.user.application.port.in.SignUpByEmailUseCase;
import net.akive.api.user.application.port.out.GoalRepository;
import net.akive.api.user.application.port.out.NicknameGenerator;
import net.akive.api.user.application.port.out.UserRepository;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;

@Service
@RequiredArgsConstructor
class SignUpByEmailService implements SignUpByEmailUseCase {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final NicknameGenerator nicknameGenerator;
    private final PasswordEncoder passwordEncoder;

    private final ValidateEmailVerificationUseCase validateEmailVerificationUseCase;
    private final CreateDefaultMentoringProfileUseCase createDefaultMentoringProfileUseCase;
    private final IssueTokenUseCase issueTokenUseCase;

    @Override
    @Transactional
    public SignUpResult signUp(SignUpByEmailCommand command) {
        validateEmailVerificationUseCase.validateVerified(command.email(), SIGN_UP);

        String nickname = nicknameGenerator.generate(command.roleType());
        String encodedPassword = passwordEncoder.encode(command.password());

        User savedUser = userRepository.save(
                User.signUpByEmail(
                        command.email(),
                        nickname,
                        encodedPassword,
                        command.roleType()
                )
        );
        Long userId = savedUser.getUserId();
        TokenIssueResult issuedToken = issueTokenUseCase.issue(userId, savedUser.getRoleType());

        goalRepository.save(Goal.createDefault(userId));
        createDefaultMentoringProfileUseCase.create(userId);

        return new SignUpResult(nickname, issuedToken.accessToken(), issuedToken.refreshToken());
    }
}
