package net.akive.api.mentoring.application.service;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.mentoring.application.required.MentoringProfileRepository;
import net.akive.api.mentoring.domain.MentoringProfile;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MentoringProfileServiceTest {

    @Mock
    UserFinder userFinder;

    @Mock
    MentoringProfileRepository mentoringProfileRepository;

    @InjectMocks
    MentoringProfileService mentoringProfileService;

    @Test
    void 멘토링_프로필을_생성한다() {
        User user = createUser(USER_ID);
        MentoringProfile savedMentoringProfile = MentoringProfile.create(user);

        given(userFinder.findByUserIdAndLeaveFalse(USER_ID)).willReturn(Optional.of(user));
        given(mentoringProfileRepository.save(any(MentoringProfile.class)))
                .willReturn(savedMentoringProfile);

        MentoringProfile result = mentoringProfileService.create(USER_ID);

        assertThat(result).isSameAs(savedMentoringProfile);

        then(userFinder).should().findByUserIdAndLeaveFalse(USER_ID);
        then(mentoringProfileRepository)
                .should()
                .save(argThat(mentoringProfile ->
                        mentoringProfile.getUser().equals(user)
                                && mentoringProfile.getDetail() == null
                ));
    }

    @Test
    void 회원이_존재하지_않으면_멘토링_프로필을_생성하지_않는다() {
        given(userFinder.findByUserIdAndLeaveFalse(UNKNOWN_USER_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> mentoringProfileService.create(UNKNOWN_USER_ID))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(mentoringProfileRepository).should(never()).save(any());
    }
}
