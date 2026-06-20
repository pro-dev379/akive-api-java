package net.akive.api.user.application.port.in;

import net.akive.api.user.application.dto.SignUpResult;
import net.akive.api.user.application.dto.SignUpByEmailCommand;

public interface SignUpByEmailUseCase {

    SignUpResult signUp(SignUpByEmailCommand command);
}
