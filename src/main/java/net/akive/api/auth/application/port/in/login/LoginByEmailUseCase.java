package net.akive.api.auth.application.port.in.login;

import net.akive.api.auth.application.dto.command.login.LoginByEmailCommand;
import net.akive.api.auth.application.dto.result.LoginResult;

public interface LoginByEmailUseCase {

    LoginResult login(LoginByEmailCommand command);
}
