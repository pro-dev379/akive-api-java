package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.event.UserSignedUpEvent;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAccountService implements UserAccountManager {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;

    private final UserSignUpDomainService userSignUpDomainService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public User signUpMemberByEmail(UserSignUpByEmailCommand command)
            throws BusinessRuleViolationException, ResourceConflictException {
        User user = userSignUpDomainService.signUpMemberByEmail(
                command.email(),
                command.password(),
                command.nickname()
        );

        User savedUser = userRepository.save(user);

        goalRepository.save(Goal.createDefault(savedUser));

        applicationEventPublisher.publishEvent(new UserSignedUpEvent(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getUserDetail().nickname()
        ));

        return savedUser;
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String passwordHash)
            throws BusinessRuleViolationException, ResourceNotFoundException {
        User user = userRepository.findByUserIdAndLeaveFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        user.updatePasswordHash(passwordHash);

        userRepository.save(user);
    }
}
