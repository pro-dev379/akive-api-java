package net.akive.api.user.application.provided;

import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;
import net.akive.api.user.domain.User;

public interface UserAccountManager {

    User signUpMemberByEmail(UserSignUpByEmailCommand command);

    void updatePassword(Long userId, String passwordHash);

}
