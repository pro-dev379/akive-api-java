package net.akive.api.auth.application.password.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.akive.api.auth.application.emailverification.provided.EmailVerificationChecker;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationEnsureCommand;
import net.akive.api.auth.application.password.provided.PasswordModifier;
import net.akive.api.auth.application.password.provided.dto.PasswordResetCommand;
import net.akive.api.auth.application.password.provided.dto.PasswordUpdateCommand;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.logging.SecurityEventType;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.PASSWORD_RESET;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_EMAIL_OR_PASSWORD;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.common.logging.SecurityEventOutcome.SUCCEEDED;
import static net.akive.api.common.logging.SecurityEventType.PASSWORD_CHANGED;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "SECURITY_EVENT")
class PasswordCommandService implements PasswordModifier {

    private final UserFinder userFinder;
    private final UserAccountManager userAccountManager;
    private final EmailVerificationChecker emailVerificationChecker;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void update(PasswordUpdateCommand command)
            throws ResourceNotFoundException, AuthenticationException {
        User user = findUserOrThrow(command.userId());

        validatePassword(command.currentPassword(), user.getPasswordHash());

        String targetPasswordHash = passwordEncoder.encode(command.targetPassword());

        userAccountManager.updatePasswordByUserId(user.getUserId(), targetPasswordHash);

        log.atInfo()
                .addKeyValue("eventType", PASSWORD_CHANGED.name())
                .addKeyValue("outcome", SUCCEEDED.name())
                .addKeyValue("userId", user.getUserId())
                .log("Member password changed.");
    }

    @Override
    public void reset(PasswordResetCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException {
        ensureEmailVerified(command.email());

        String targetPasswordHash = passwordEncoder.encode(command.targetPassword());

        userAccountManager.updatePasswordByEmail(command.email(), targetPasswordHash);

        log.atInfo()
                .addKeyValue("eventType", SecurityEventType.PASSWORD_RESET.name())
                .addKeyValue("outcome", SUCCEEDED.name())
                .log("Member password reset.");
    }

    private User findUserOrThrow(Long userId) {
        return userFinder.find(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    private void validatePassword(String rawPassword, String passwordHash) {
        if (!passwordEncoder.matches(rawPassword, passwordHash)) {
            throw new AuthenticationException(INVALID_EMAIL_OR_PASSWORD);
        }
    }

    private void ensureEmailVerified(Email email) {
        EmailVerificationEnsureCommand command = new EmailVerificationEnsureCommand(email, PASSWORD_RESET);

        emailVerificationChecker.ensureEmailVerified(command);
    }
}
