package net.akive.api.auth.application.password.provided;

import net.akive.api.auth.application.password.provided.dto.PasswordUpdateCommand;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;

public interface PasswordModifier {

    void update(PasswordUpdateCommand command) throws ResourceNotFoundException, AuthenticationException;
}
