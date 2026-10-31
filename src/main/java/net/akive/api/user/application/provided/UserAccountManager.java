package net.akive.api.user.application.provided;

import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.dto.UserDetailUpdateCommand;
import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;
import net.akive.api.user.application.provided.dto.UserWithdrawCommand;
import net.akive.api.user.domain.User;

public interface UserAccountManager {

    User signUpMemberByEmail(UserSignUpByEmailCommand command)
            throws BusinessRuleViolationException, ResourceConflictException, ResourceNotFoundException;

    void updateUserDetail(UserDetailUpdateCommand command)
            throws ResourceNotFoundException, ResourceConflictException;

    void updatePasswordByUserId(Long userId, String targetPasswordHash)
            throws BusinessRuleViolationException, ResourceNotFoundException;

    void updatePasswordByEmail(Email email, String targetPasswordHash)
            throws BusinessRuleViolationException, ResourceNotFoundException;

    void withdraw(UserWithdrawCommand command) throws ResourceNotFoundException;
}
