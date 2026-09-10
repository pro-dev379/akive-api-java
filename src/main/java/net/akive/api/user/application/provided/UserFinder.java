package net.akive.api.user.application.provided;

import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.domain.User;

import java.util.Optional;

public interface UserFinder {

    Optional<User> findByUserIdAndNotWithdrawn(Long userId);

    User findOrThrowByUserIdAndNotWithdrawn(Long userId) throws ResourceNotFoundException;

    Optional<User> findByEmailAndNotWithdrawn(Email email);
}
