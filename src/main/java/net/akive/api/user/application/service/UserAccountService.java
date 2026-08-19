package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.mentoring.application.provided.MentoringProfileCreator;
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

import static net.akive.api.common.exception.business.NotFoundError.USER_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAccountService implements UserAccountManager {

    private final MentoringProfileCreator mentoringProfileCreator;

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;

    private final UserSignUpDomainService userSignUpDomainService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public User signUpMemberByEmail(UserSignUpByEmailCommand command) throws BadRequestException, ConflictException {
        User user = userSignUpDomainService.signUpMemberByEmail(
                command.email(),
                command.password(),
                command.nickname()
        );

        User savedUser = userRepository.save(user);

        goalRepository.save(Goal.createDefault(savedUser));

        mentoringProfileCreator.create(savedUser.getUserId());

        applicationEventPublisher.publishEvent(new UserSignedUpEvent(
                savedUser.getEmail(),
                savedUser.getUserDetail().nickname()
        ));

        return savedUser;
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String passwordHash) throws BadRequestException, NotFoundException {
        User user = userRepository.findByUserIdAndLeaveFalse(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        user.updatePasswordHash(passwordHash);

        userRepository.save(user);
    }
}
