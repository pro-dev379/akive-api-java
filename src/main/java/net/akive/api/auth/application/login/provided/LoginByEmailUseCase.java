package net.akive.api.auth.application.login.provided;

import net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand;
import net.akive.api.auth.application.login.provided.dto.LoginResult;

public interface LoginByEmailUseCase {

    LoginResult login(LoginByEmailCommand command);
}
