package net.akive.api.user.application.service;

import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceConflictError.EMAIL_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceConflictError.NICKNAME_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.fixture.UserDetailFixture.NICKNAME;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserValidationServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserValidationService userValidationService;

    @Test
    void 이메일로_회원_존재_검증에_성공한다() {
        User user = createUser();
        given(userRepository.findByEmailAndNotWithdrawn(EMAIL)).willReturn(Optional.of(user));

        userValidationService.validateUserExistsByEmail(EMAIL);

        then(userRepository).should().findByEmailAndNotWithdrawn(EMAIL);
    }

    @Test
    void 이메일에_해당하는_탈퇴하지_않은_회원이_없으면_회원_존재_검증에_실패한다() {
        given(userRepository.findByEmailAndNotWithdrawn(EMAIL)).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> userValidationService.validateUserExistsByEmail(EMAIL))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userRepository).should().findByEmailAndNotWithdrawn(EMAIL);
    }

    @Test
    void 이메일_중복_검증에_성공한다() {
        given(userRepository.existsByEmail(EMAIL)).willReturn(false);

        userValidationService.validateEmailNotDuplicate(EMAIL);

        then(userRepository).should().existsByEmail(EMAIL);
    }

    @Test
    void 이미_존재하는_이메일이면_이메일_중복_검증에_실패한다() {
        given(userRepository.existsByEmail(EMAIL)).willReturn(true);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> userValidationService.validateEmailNotDuplicate(EMAIL))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(EMAIL_ALREADY_EXISTS);

        then(userRepository).should().existsByEmail(EMAIL);
    }

    @Test
    void 닉네임_중복_검증에_성공한다() {
        given(userRepository.existsByNickname(NICKNAME)).willReturn(false);

        userValidationService.validateNicknameNotDuplicate(NICKNAME);

        then(userRepository).should().existsByNickname(NICKNAME);
    }

    @Test
    void 이미_존재하는_닉네임이면_닉네임_중복_검증에_실패한다() {
        given(userRepository.existsByNickname(NICKNAME)).willReturn(true);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> userValidationService.validateNicknameNotDuplicate(NICKNAME))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(NICKNAME_ALREADY_EXISTS);

        then(userRepository).should().existsByNickname(NICKNAME);
    }

    @Test
    void 자기_자신을_제외한_닉네임_중복_검증에_성공한다() {
        given(userRepository.existsByNicknameNotAndUserIdNot(NICKNAME, USER_ID)).willReturn(false);

        userValidationService.validateNicknameNotDuplicateExcludingUser(NICKNAME, USER_ID);

        then(userRepository).should().existsByNicknameNotAndUserIdNot(NICKNAME, USER_ID);
    }

    @Test
    void 자기_자신을_제외하고도_이미_존재하는_닉네임이면_닉네임_중복_검증에_실패한다() {
        given(userRepository.existsByNicknameNotAndUserIdNot(NICKNAME, USER_ID)).willReturn(true);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> userValidationService.validateNicknameNotDuplicateExcludingUser(NICKNAME, USER_ID))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(NICKNAME_ALREADY_EXISTS);

        then(userRepository).should().existsByNicknameNotAndUserIdNot(NICKNAME, USER_ID);
    }
}
