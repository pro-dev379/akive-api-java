package net.akive.api.user.application.service;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.MAX_LENGTH_EXCEEDED;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.UserPolicy.NICKNAME_MAX_LENGTH;
import static net.akive.api.user.domain.fixture.UserDetailFixture.NICKNAME;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserQueryService userQueryService;

    @Test
    void 회원_식별자로_탈퇴하지_않은_회원을_조회해서_반환한다() {
        User user = createReconstructedUser();
        given(userRepository.findByUserIdAndNotWithdrawn(USER_ID)).willReturn(Optional.of(user));

        User foundUser = userQueryService.findOrThrow(USER_ID);

        assertThat(foundUser).isEqualTo(user);

        then(userRepository).should().findByUserIdAndNotWithdrawn(USER_ID);
    }

    @Test
    void 회원_식별자로_탈퇴하지_않은_회원을_조회할_수_없으면_예외가_발생한다() {
        given(userRepository.findByUserIdAndNotWithdrawn(UNKNOWN_USER_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> userQueryService.findOrThrow(UNKNOWN_USER_ID))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userRepository).should().findByUserIdAndNotWithdrawn(UNKNOWN_USER_ID);
    }

    @Test
    void 회원_식별자로_탈퇴하지_않은_회원을_Optional로_조회한다() {
        User user = createReconstructedUser();
        given(userRepository.findByUserIdAndNotWithdrawn(USER_ID)).willReturn(Optional.of(user));

        Optional<User> foundUser = userQueryService.find(USER_ID);

        assertThat(foundUser).contains(user);

        then(userRepository).should().findByUserIdAndNotWithdrawn(USER_ID);
    }

    @Test
    void 이메일로_탈퇴하지_않은_회원을_Optional로_조회한다() {
        User user = createReconstructedUser();
        given(userRepository.findByEmailAndNotWithdrawn(EMAIL)).willReturn(Optional.of(user));

        Optional<User> foundUser = userQueryService.findByEmail(EMAIL);

        assertThat(foundUser).contains(user);

        then(userRepository).should().findByEmailAndNotWithdrawn(EMAIL);
    }

    @Test
    void 닉네임에_해당하는_회원이_존재하는지_확인한다() {
        given(userRepository.existsByNickname(NICKNAME)).willReturn(true);

        boolean exists = userQueryService.existsByNickname(NICKNAME);

        assertThat(exists).isTrue();
        then(userRepository).should().existsByNickname(NICKNAME);
    }

    @Test
    void 닉네임_존재_여부_확인_시_닉네임을_정규화한다() {
        String nickname = " " + NICKNAME + " ";
        given(userRepository.existsByNickname(NICKNAME)).willReturn(true);

        boolean exists = userQueryService.existsByNickname(nickname);

        assertThat(exists).isTrue();
        then(userRepository).should().existsByNickname(NICKNAME);
    }

    @Test
    void 닉네임_존재_여부_확인_시_닉네임이_최대_길이를_초과하면_예외가_발생한다() {
        String nickname = "a".repeat(NICKNAME_MAX_LENGTH + 1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> userQueryService.existsByNickname(nickname))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);

        then(userRepository).shouldHaveNoInteractions();
    }

    @Test
    void 자기_자신을_제외하고_닉네임에_해당하는_회원이_존재하는지_확인한다() {
        given(userRepository.existsByNicknameNotAndUserIdNot(NICKNAME, USER_ID)).willReturn(true);

        boolean exists = userQueryService.existsByNicknameExcludingUser(NICKNAME, USER_ID);

        assertThat(exists).isTrue();
        then(userRepository).should().existsByNicknameNotAndUserIdNot(NICKNAME, USER_ID);
    }

    @Test
    void 자기_자신을_제외한_닉네임_존재_여부_확인_시_닉네임과_회원_식별자를_정규화한다() {
        String nickname = " " + NICKNAME + " ";
        given(userRepository.existsByNicknameNotAndUserIdNot(NICKNAME, USER_ID)).willReturn(true);

        boolean exists = userQueryService.existsByNicknameExcludingUser(nickname, USER_ID);

        assertThat(exists).isTrue();
        then(userRepository).should().existsByNicknameNotAndUserIdNot(NICKNAME, USER_ID);
    }

    @Test
    void 자기_자신을_제외한_닉네임_존재_여부_확인_시_닉네임이_최대_길이를_초과하면_예외가_발생한다() {
        String nickname = "a".repeat(NICKNAME_MAX_LENGTH + 1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> userQueryService.existsByNicknameExcludingUser(nickname, USER_ID))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);

        then(userRepository).shouldHaveNoInteractions();
    }
}
