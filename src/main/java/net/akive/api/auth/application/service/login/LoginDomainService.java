package net.akive.api.auth.application.service.login;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.domain.login.LoginUser;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class LoginDomainService {

    private final LoginPasswordValidator loginPasswordValidator;

    LoginAuthentication authenticate(LoginUser user, String rawPassword) {
        boolean legacyPasswordUsed = loginPasswordValidator.validateAndCheckLegacyPasswordUsed(user, rawPassword);

        return new LoginAuthentication(user, legacyPasswordUsed);
    }

    record LoginAuthentication(
            LoginUser user,
            boolean legacyPasswordUsed
    ) {
    }
}
