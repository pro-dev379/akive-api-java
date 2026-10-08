package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.user.application.provided.UserValidator;
import net.akive.api.user.application.required.NicknameGenerator;
import net.akive.api.user.application.required.UserEmailVerificationChecker;
import net.akive.api.user.domain.UserRole;
import net.akive.api.user.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static net.akive.api.user.domain.UserRole.MEMBER;

@Component
@RequiredArgsConstructor
class UserSignUpDomainService {

    private final UserValidator userValidator;

    private final NicknameGenerator nicknameGenerator;
    private final UserEmailVerificationChecker userEmailVerificationChecker;

    private final PasswordEncoder passwordEncoder;

    User signUpMemberByEmail(Email email, String password, String nickname) {
        UserRole role = MEMBER;

        SignUpNickname signUpNickname = prepareNickname(role, nickname);

        validateEmailSignUpAvailable(email, signUpNickname);

        String passwordHash = passwordEncoder.encode(password);

        return User.signUpByEmail(email, role, passwordHash, signUpNickname.value());
    }

    private SignUpNickname prepareNickname(UserRole role, String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return SignUpNickname.generated(nicknameGenerator.generate(role));
        }

        return SignUpNickname.providedByUser(nickname.trim());
    }

    private void validateEmailSignUpAvailable(Email email, SignUpNickname signUpNickname) {
        userValidator.validateEmailNotDuplicate(email);

        userEmailVerificationChecker.ensureEmailVerifiedForSignUp(email);

        if (signUpNickname.isProvidedByUser()) {
            userValidator.validateNicknameNotDuplicate(signUpNickname.value());
        }
    }

    private record SignUpNickname(String value, boolean isProvidedByUser) {

        static SignUpNickname providedByUser(String value) {
            return new SignUpNickname(value, true);
        }

        static SignUpNickname generated(String value) {
            return new SignUpNickname(value, false);
        }
    }
}
