package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.user.application.port.out.GenerateNicknamePort;
import net.akive.api.user.domain.User;
import net.akive.api.user.domain.RoleType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SignUpDomainService {

    private final SignUpPolicy signUpPolicy;
    private final GenerateNicknamePort generateNicknamePort;
    private final PasswordEncoder passwordEncoder;

    User signUpByEmail(Email email, String password, RoleType roleType) {
        signUpPolicy.validateEmailSignUpAvailable(email);

        String nickname = generateNicknamePort.generate(roleType);
        String encodedPassword = passwordEncoder.encode(password);

        return User.signUpByEmail(
                email,
                nickname,
                encodedPassword,
                roleType
        );
    }
}
