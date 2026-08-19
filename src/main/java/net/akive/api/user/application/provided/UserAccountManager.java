package net.akive.api.user.application.provided;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;
import net.akive.api.user.domain.User;

public interface UserAccountManager {

    User signUpMemberByEmail(UserSignUpByEmailCommand command) throws BadRequestException, ConflictException;

    void updatePassword(Long userId, String passwordHash) throws BadRequestException, NotFoundException;

}
