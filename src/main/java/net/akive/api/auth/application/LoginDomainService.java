package net.akive.api.auth.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.domain.login.LoginUser;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class LoginDomainService {

    private final LoginPasswordPolicy loginPasswordPolicy;

    LoginAuthentication authenticate(LoginUser user, String rawPassword) {
        boolean legacyPasswordUsed = loginPasswordPolicy.validateAndCheckLegacyPasswordUsed(user, rawPassword);

        return new LoginAuthentication(user, legacyPasswordUsed);
    }

    record LoginAuthentication(
            LoginUser user,
            boolean legacyPasswordUsed
    ) {
    }
}
