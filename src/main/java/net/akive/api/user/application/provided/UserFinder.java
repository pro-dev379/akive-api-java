package net.akive.api.user.application.provided;

import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.domain.User;
import net.akive.api.user.domain.WithdrawalReason;

import java.util.List;
import java.util.Optional;

public interface UserFinder {

    Optional<User> find(Long userId);

    User findOrThrow(Long userId) throws ResourceNotFoundException;

    Optional<User> findByEmail(Email email);

    List<WithdrawalReason> findWithdrawalReasons();
}
