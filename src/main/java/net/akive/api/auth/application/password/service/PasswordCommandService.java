package net.akive.api.auth.application.password.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.password.provided.PasswordModifier;
import net.akive.api.auth.application.password.provided.dto.PasswordUpdateCommand;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.AuthenticationError.INVALID_EMAIL_OR_PASSWORD;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
class PasswordCommandService implements PasswordModifier {

    private final UserFinder userFinder;
    private final UserAccountManager userAccountManager;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void update(PasswordUpdateCommand command)
            throws ResourceNotFoundException, AuthenticationException {
        User user = findUserOrThrow(command.userId());

        validatePassword(command.currentPassword(), user.getPasswordHash());

        String targetPasswordHash = passwordEncoder.encode(command.targetPassword());

        userAccountManager.updatePassword(user.getUserId(), targetPasswordHash);
    }

    private User findUserOrThrow(Long userId) {
        return userFinder.findByUserIdAndNotWithdrawn(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    private void validatePassword(String rawPassword, String passwordHash) {
        if (!passwordEncoder.matches(rawPassword, passwordHash)) {
            throw new AuthenticationException(INVALID_EMAIL_OR_PASSWORD);
        }
    }
}
