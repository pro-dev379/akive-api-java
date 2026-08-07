package net.akive.api.mentoring.application.service;

import net.akive.api.mentoring.application.required.SaveMentoringProfilePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CreateDefaultMentoringProfileServiceTest {

    @Mock
    SaveMentoringProfilePort saveMentoringProfilePort;

    @InjectMocks
    CreateDefaultMentoringProfileService createDefaultMentoringProfileService;

    @Test
    void 회원의_기본_멘토링_프로필을_생성하여_저장한다() {
        // given
        Long userId = 1L;

        // when
        createDefaultMentoringProfileService.create(userId);

        // then
        then(saveMentoringProfilePort)
                .should()
                .save(argThat(mentoringProfile ->
                        mentoringProfile.getUserId().equals(userId)
                ));
    }
}
