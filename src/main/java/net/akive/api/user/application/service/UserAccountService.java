package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.image.application.provided.ImageAssetReader;
import net.akive.api.user.application.event.UserSignedUpEvent;
import net.akive.api.user.application.event.UserWithdrawnEvent;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserValidator;
import net.akive.api.user.application.provided.dto.UserDetailUpdateCommand;
import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;
import net.akive.api.user.application.provided.dto.UserWithdrawCommand;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.IMAGE_ASSET_NOT_FOUND;
import static net.akive.api.image.domain.ImageType.PROFILE_IMAGE;

@Service
@Transactional
@RequiredArgsConstructor
class UserAccountService implements UserAccountManager {

    private final UserValidator userValidator;
    private final ImageAssetReader imageAssetReader;

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;

    private final UserSignUpDomainService userSignUpDomainService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CurrentTimeProvider currentTimeProvider;

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
    public void updateUserDetail(UserDetailUpdateCommand command)
            throws ResourceNotFoundException, ResourceConflictException {
        User user = findNotWithdrawnUserByUserIdOrThrow(command.userId());

        userValidator.validateNicknameNotDuplicateExcludingUser(command.targetNickname(), command.userId());
        validateProfileImageAssetExists(command.targetProfileImageAssetId());

        user.updateUserDetail(
                command.targetNickname(),
                command.targetProfileImageAssetId(),
                command.targetAim()
        );

        userRepository.save(user);
    }

    @Override
    public void updatePasswordByUserId(Long userId, String targetPasswordHash)
            throws BusinessRuleViolationException, ResourceNotFoundException {
        User user = findNotWithdrawnUserByUserIdOrThrow(userId);

        user.updatePasswordHash(targetPasswordHash);

        userRepository.save(user);
    }

    @Override
    public void updatePasswordByEmail(Email email, String targetPasswordHash)
            throws BusinessRuleViolationException, ResourceNotFoundException {
        User user = findNotWithdrawnUserByEmailOrThrow(email);

        user.updatePasswordHash(targetPasswordHash);

        userRepository.save(user);
    }

    @Override
    public void withdraw(UserWithdrawCommand command) throws ResourceNotFoundException {
        User user = findNotWithdrawnUserByUserIdOrThrow(command.userId());

        user.withdraw(currentTimeProvider.now(), command.withdrawReason());

        userRepository.save(user);

        applicationEventPublisher.publishEvent(new UserWithdrawnEvent(command.userId()));
    }

    private User findNotWithdrawnUserByUserIdOrThrow(Long userId) {
        return userRepository.findByUserIdAndNotWithdrawn(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    private void validateProfileImageAssetExists(UUID imageAssetId) {
        if (imageAssetId == null) {
            return;
        }

        imageAssetReader.findForUpdate(imageAssetId)
                .filter(imageAsset -> imageAsset.isOfType(PROFILE_IMAGE))
                .orElseThrow(() -> new ResourceNotFoundException(IMAGE_ASSET_NOT_FOUND));
    }

    private User findNotWithdrawnUserByEmailOrThrow(Email email) {
        return userRepository.findByEmailAndNotWithdrawn(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }
}
