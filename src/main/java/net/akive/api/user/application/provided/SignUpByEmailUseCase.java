package net.akive.api.user.application.provided;

import net.akive.api.user.application.provided.dto.SignUpResult;
import net.akive.api.user.application.provided.dto.SignUpByEmailCommand;

public interface SignUpByEmailUseCase {

    SignUpResult signUp(SignUpByEmailCommand command);
}
