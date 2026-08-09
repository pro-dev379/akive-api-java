package net.akive.api.mentoring.application.service;

import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.mentoring.application.required.MentoringProfileRepository;
import net.akive.api.mentoring.domain.MentoringProfile;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.common.exception.business.NotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        Long userId = 1L;
        User user = createUser(userId);
        MentoringProfile savedMentoringProfile = MentoringProfile.create(user);

        given(userFinder.find(userId)).willReturn(user);
        given(mentoringProfileRepository.save(any(MentoringProfile.class)))
                .willReturn(savedMentoringProfile);

        MentoringProfile result = mentoringProfileService.create(userId);

        assertThat(result).isSameAs(savedMentoringProfile);

        then(userFinder).should().find(userId);
        then(mentoringProfileRepository)
                .should()
                .save(argThat(mentoringProfile ->
                        mentoringProfile.getUser().equals(user)
                                && mentoringProfile.getDetail() == null
                ));
    }

    @Test
    void 회원이_존재하지_않으면_멘토링_프로필을_생성하지_않는다() {
        Long userId = 999L;
        NotFoundException exception = new NotFoundException(USER_NOT_FOUND);

        given(userFinder.find(userId)).willThrow(exception);

        assertThatThrownBy(() -> mentoringProfileService.create(userId))
                .isSameAs(exception);

        then(mentoringProfileRepository).should(never()).save(any());
    }
}
