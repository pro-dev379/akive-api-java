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
import static net.akive.api.user.domain.UserDetailFixture.NICKNAME;
import static net.akive.api.user.domain.UserFixture.EMAIL;
import static net.akive.api.user.domain.UserFixture.createUser;
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
        given(userRepository.findByEmailAndLeaveFalse(EMAIL)).willReturn(Optional.of(user));

        userValidationService.validateUserExistsByEmail(EMAIL);

        then(userRepository).should().findByEmailAndLeaveFalse(EMAIL);
    }

    @Test
    void 이메일에_해당하는_탈퇴하지_않은_회원이_없으면_회원_존재_검증에_실패한다() {
        given(userRepository.findByEmailAndLeaveFalse(EMAIL)).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> userValidationService.validateUserExistsByEmail(EMAIL))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userRepository).should().findByEmailAndLeaveFalse(EMAIL);
    }

    @Test
    void 이메일_중복_검증에_성공한다() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

        userValidationService.validateEmailNotDuplicate(EMAIL);

        then(userRepository).should().findByEmail(EMAIL);
    }

    @Test
    void 이미_존재하는_이메일이면_이메일_중복_검증에_실패한다() {
        User user = createUser();
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> userValidationService.validateEmailNotDuplicate(EMAIL))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(EMAIL_ALREADY_EXISTS);

        then(userRepository).should().findByEmail(EMAIL);
    }

    @Test
    void 닉네임_중복_검증에_성공한다() {
        given(userRepository.findByNickname(NICKNAME)).willReturn(Optional.empty());

        userValidationService.validateNicknameNotDuplicate(NICKNAME);

        then(userRepository).should().findByNickname(NICKNAME);
    }

    @Test
    void 이미_존재하는_닉네임이면_닉네임_중복_검증에_실패한다() {
        User user = createUser();
        given(userRepository.findByNickname(NICKNAME)).willReturn(Optional.of(user));

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> userValidationService.validateNicknameNotDuplicate(NICKNAME))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(NICKNAME_ALREADY_EXISTS);

        then(userRepository).should().findByNickname(NICKNAME);
    }
}
