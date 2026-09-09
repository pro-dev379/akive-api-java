package net.akive.api.user.application.service;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.event.UserSignedUpEvent;
import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.EMAIL_NOT_VERIFIED;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.application.fixture.UserAccountCommandFixture.createUserSignUpByEmailCommand;
import static net.akive.api.user.domain.UserFixture.*;
import static net.akive.api.user.domain.UserPolicy.*;
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
    UserSignUpDomainService userSignUpDomainService;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

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
    void 회원_가입_도메인_처리에_실패하면_회원과_기본_데이터를_생성하지_않고_웰컴_메일_전송_이벤트도_발행하지_않는다() {
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
    void 패스워드를_변경한다() {
        User user = createReconstructedUser(USER_ID, null);

        given(userRepository.findByUserIdAndLeaveFalse(USER_ID)).willReturn(Optional.of(user));

        userAccountService.updatePassword(USER_ID, PASSWORD_HASH);

        assertThat(user.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        then(userRepository).should().findByUserIdAndLeaveFalse(USER_ID);
        then(userRepository).should().save(user);
    }

    @Test
    void 패스워드를_변경할_회원이_없으면_실패한다() {
        given(userRepository.findByUserIdAndLeaveFalse(USER_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> userAccountService.updatePassword(USER_ID, PASSWORD_HASH))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userRepository).should().findByUserIdAndLeaveFalse(USER_ID);
        then(userRepository).should(never()).save(any(User.class));
    }
}
