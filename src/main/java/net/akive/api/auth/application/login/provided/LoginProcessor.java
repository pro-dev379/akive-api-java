package net.akive.api.auth.application.login.provided;

import net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand;
import net.akive.api.auth.domain.token.IssuedToken;

public interface LoginProcessor {

    IssuedToken loginByEmail(LoginByEmailCommand command);
}
