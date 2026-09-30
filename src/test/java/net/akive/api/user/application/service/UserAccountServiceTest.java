package net.akive.api.user.application.service;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.file.application.provided.FileManager;
import net.akive.api.user.application.event.UserSignedUpEvent;
import net.akive.api.user.application.event.UserWithdrawnEvent;
import net.akive.api.user.application.provided.UserValidator;
import net.akive.api.user.application.provided.dto.UserDetailUpdateCommand;
import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;
import net.akive.api.user.application.provided.dto.UserWithdrawCommand;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import net.akive.api.user.domain.UserDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.EMAIL_NOT_VERIFIED;
import static net.akive.api.common.exception.business.ResourceConflictError.NICKNAME_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.application.fixture.UserAccountCommandFixture.*;
import static net.akive.api.user.domain.UserPolicy.*;
import static net.akive.api.user.domain.fixture.UserDetailFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static net.akive.api.user.domain.fixture.WithdrawalFixture.WITHDRAWN_AT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    GoalRepository goalRepository;

    @Mock
    FileManager fileManager;

    @Mock
    UserSignUpDomainService userSignUpDomainService;

    @Mock
    UserValidator userValidator;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @Mock
    CurrentTimeProvider currentTimeProvider;

    @InjectMocks
    UserAccountService userAccountService;

    @Test
    void 이메일로_회원을_가입시키고_기본_목표를_생성한_뒤_회원_가입_이벤트를_발행한다() {
        UserSignUpByEmailCommand command = createUserSignUpByEmailCommand();
        User user = createUser();
        User savedUser = createReconstructedUser();

        given(userSignUpDomainService.signUpMemberByEmail(command.email(), command.password(), command.nickname()))
                .willReturn(user);
        given(userRepository.save(user)).willReturn(savedUser);

        User result = userAccountService.signUpMemberByEmail(command);

        assertThat(result).isEqualTo(savedUser);

        then(userSignUpDomainService).should()
                .signUpMemberByEmail(command.email(), command.password(), command.nickname());
        then(userRepository).should().save(user);

        ArgumentCaptor<Goal> goalCaptor = ArgumentCaptor.forClass(Goal.class);
        then(goalRepository).should().save(goalCaptor.capture());
        assertThat(goalCaptor.getValue().getUser()).isEqualTo(savedUser);
        assertThat(goalCaptor.getValue().getAttendOn()).isEqualTo(DEFAULT_GOAL_ATTEND_ON);
        assertThat(goalCaptor.getValue().getRest10MinSec()).isEqualTo(DEFAULT_GOAL_REST_10_MIN_SEC);
        assertThat(goalCaptor.getValue().getMealNum()).isEqualTo(DEFAULT_GOAL_MEAL_NUM);
        assertThat(goalCaptor.getValue().getMeal1TimeSec()).isEqualTo(DEFAULT_GOAL_MEAL_1_TIME_SEC);

        ArgumentCaptor<UserSignedUpEvent> eventCaptor = ArgumentCaptor.forClass(UserSignedUpEvent.class);
        then(applicationEventPublisher).should().publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userId()).isEqualTo(savedUser.getUserId());
        assertThat(eventCaptor.getValue().email()).isEqualTo(savedUser.getEmail());
        assertThat(eventCaptor.getValue().nickname()).isEqualTo(savedUser.getUserDetail().nickname());
    }

    @Test
    void 회원_가입_도메인_처리에_실패하면_회원과_기본_데이터를_생성하지_않고_회원_가입_이벤트도_발행하지_않는다() {
        UserSignUpByEmailCommand command = createUserSignUpByEmailCommand();

        willThrow(new BusinessRuleViolationException(EMAIL_NOT_VERIFIED))
                .given(userSignUpDomainService)
                .signUpMemberByEmail(command.email(), command.password(), command.nickname());

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> userAccountService.signUpMemberByEmail(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(EMAIL_NOT_VERIFIED);

        then(userRepository).should(never()).save(any(User.class));
        then(goalRepository).shouldHaveNoInteractions();
        then(applicationEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    void 회원_상세_정보를_수정한다() {
        UserDetailUpdateCommand command = createUserDetailUpdateCommand();
        User user = createReconstructedUser(command.userId());

        given(userRepository.findByUserIdAndNotWithdrawn(command.userId())).willReturn(Optional.of(user));

        userAccountService.updateUserDetail(command);

        assertThat(user.getUserDetail().nickname()).isEqualTo(command.targetNickname());
        assertThat(user.getUserDetail().profileImageAssetId())
                .isEqualTo(command.targetProfileImageAssetId());
        assertThat(user.getUserDetail().aim()).isEqualTo(command.targetAim());

        then(userRepository).should().findByUserIdAndNotWithdrawn(command.userId());
        then(userValidator).should()
                .validateNicknameNotDuplicateExcludingUser(command.targetNickname(), command.userId());
        then(userRepository).should().save(user);
        then(fileManager).shouldHaveNoInteractions();
    }

    @Test
    void 회원_상세_정보_수정_시_프로필_이미지_에셋이_변경되면_기존_이미지_에셋을_삭제한다() {
        UUID existingProfileImageAssetId = PROFILE_IMAGE_ASSET_ID;
        UUID targetProfileImageAssetId = ANOTHER_PROFILE_IMAGE_ASSET_ID;
        UserDetailUpdateCommand command = new UserDetailUpdateCommand(
                USER_ID,
                NICKNAME,
                targetProfileImageAssetId,
                AIM
        );
        User user = createReconstructedUser(command.userId(), createUserDetail(existingProfileImageAssetId));

        given(userRepository.findByUserIdAndNotWithdrawn(command.userId())).willReturn(Optional.of(user));

        userAccountService.updateUserDetail(command);

        assertThat(user.getUserDetail().profileImageAssetId()).isEqualTo(targetProfileImageAssetId);
        then(userRepository).should().save(user);
        then(fileManager).should().deleteImageAsset(existingProfileImageAssetId);
    }

    @Test
    void 회원_상세_정보_수정_시_프로필_이미지_에셋이_기존과_동일하면_기존_이미지_에셋을_삭제하지_않는다() {
        UserDetailUpdateCommand command = createUserDetailUpdateCommand();
        UserDetail existingUserDetail = createUserDetail(command.targetProfileImageAssetId());
        User user = createReconstructedUser(command.userId(), existingUserDetail);

        given(userRepository.findByUserIdAndNotWithdrawn(command.userId())).willReturn(Optional.of(user));

        userAccountService.updateUserDetail(command);

        then(userRepository).should().save(user);
        then(fileManager).shouldHaveNoInteractions();
    }

    @Test
    void 상세_정보를_수정할_회원이_없으면_회원_상세_정보를_저장하지_않는다() {
        UserDetailUpdateCommand command = createUserDetailUpdateCommand(UNKNOWN_USER_ID);

        given(userRepository.findByUserIdAndNotWithdrawn(command.userId())).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> userAccountService.updateUserDetail(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userRepository).should().findByUserIdAndNotWithdrawn(command.userId());
        then(userValidator).shouldHaveNoInteractions();
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    void 자기_자신을_제외하고도_이미_존재하는_닉네임이면_회원_상세_정보를_수정하지_않고_저장하지_않는다() {
        UserDetailUpdateCommand command = createUserDetailUpdateCommand();
        User user = createReconstructedUser(command.userId());

        given(userRepository.findByUserIdAndNotWithdrawn(command.userId())).willReturn(Optional.of(user));
        willThrow(new ResourceConflictException(NICKNAME_ALREADY_EXISTS))
                .given(userValidator)
                .validateNicknameNotDuplicateExcludingUser(command.targetNickname(), command.userId());

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> userAccountService.updateUserDetail(command))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(NICKNAME_ALREADY_EXISTS);

        then(userRepository).should().findByUserIdAndNotWithdrawn(command.userId());
        then(userValidator).should()
                .validateNicknameNotDuplicateExcludingUser(command.targetNickname(), command.userId());
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    void 회원_식별자로_패스워드를_변경한다() {
        User user = createReconstructedUser(USER_ID, PASSWORD_HASH);

        given(userRepository.findByUserIdAndNotWithdrawn(USER_ID)).willReturn(Optional.of(user));

        userAccountService.updatePasswordByUserId(USER_ID, TARGET_PASSWORD_HASH);

        assertThat(user.getPasswordHash()).isEqualTo(TARGET_PASSWORD_HASH);
        then(userRepository).should().findByUserIdAndNotWithdrawn(USER_ID);
        then(userRepository).should().save(user);
    }

    @Test
    void 회원_식별자로_패스워드를_변경할_회원이_없으면_실패한다() {
        given(userRepository.findByUserIdAndNotWithdrawn(USER_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> userAccountService.updatePasswordByUserId(USER_ID, TARGET_PASSWORD_HASH))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userRepository).should().findByUserIdAndNotWithdrawn(USER_ID);
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    void 이메일로_패스워드를_변경한다() {
        User user = createReconstructedUser(USER_ID, PASSWORD_HASH);

        given(userRepository.findByEmailAndNotWithdrawn(EMAIL)).willReturn(Optional.of(user));

        userAccountService.updatePasswordByEmail(EMAIL, TARGET_PASSWORD_HASH);

        assertThat(user.getPasswordHash()).isEqualTo(TARGET_PASSWORD_HASH);
        then(userRepository).should().findByEmailAndNotWithdrawn(EMAIL);
        then(userRepository).should().save(user);
    }

    @Test
    void 이메일로_패스워드를_변경할_회원이_없으면_실패한다() {
        given(userRepository.findByEmailAndNotWithdrawn(EMAIL)).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> userAccountService.updatePasswordByEmail(EMAIL, TARGET_PASSWORD_HASH))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userRepository).should().findByEmailAndNotWithdrawn(EMAIL);
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    void 회원을_탈퇴시키고_회원_탈퇴_이벤트를_발행한다() {
        UserWithdrawCommand command = createUserWithdrawCommand();
        User user = createReconstructedUser(command.userId());

        given(currentTimeProvider.now()).willReturn(WITHDRAWN_AT);
        given(userRepository.findByUserIdAndNotWithdrawn(command.userId())).willReturn(Optional.of(user));

        userAccountService.withdraw(command);

        assertThat(user.isWithdrawn()).isTrue();
        assertThat(user.getWithdrawal().withdrawnAt()).isEqualTo(WITHDRAWN_AT);
        assertThat(user.getWithdrawal().withdrawalReason()).isEqualTo(command.withdrawReason());

        then(userRepository).should().save(user);

        ArgumentCaptor<UserWithdrawnEvent> eventCaptor = ArgumentCaptor.forClass(UserWithdrawnEvent.class);
        then(applicationEventPublisher).should().publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userId()).isEqualTo(command.userId());
    }

    @Test
    void 탈퇴시킬_회원이_없으면_회원_탈퇴_처리를_저장하지_않고_회원_탈퇴_이벤트도_발행하지_않는다() {
        UserWithdrawCommand command = createUserWithdrawCommand(UNKNOWN_USER_ID);

        given(userRepository.findByUserIdAndNotWithdrawn(command.userId())).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> userAccountService.withdraw(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userRepository).should().findByUserIdAndNotWithdrawn(command.userId());
        then(currentTimeProvider).shouldHaveNoInteractions();
        then(userRepository).should(never()).save(any(User.class));
        then(applicationEventPublisher).shouldHaveNoInteractions();
    }
}
