package net.akive.api.coaching.application.service;

import net.akive.api.coaching.application.provided.dto.CoachingConversationCreateCommand;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.application.required.CoachingLlm;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.coaching.application.fixture.CoachingConversationCommandFixture.createCoachingConversationCreateCommand;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.*;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CoachingConversationCommandServiceTest {

    @Mock
    UserFinder userFinder;

    @Mock
    CoachingLlm coachingLlm;

    @Mock
    CoachingConversationRepository coachingConversationRepository;

    @InjectMocks
    CoachingConversationCommandService coachingConversationCommandService;

    @Test
    void 코칭_대화를_생성한다() {
        User user = createReconstructedUser(USER_ID);
        CoachingConversationCreateCommand command = createCoachingConversationCreateCommand(USER_ID);
        CoachingConversation savedCoachingConversation = reconstructCoachingConversation(
                COACHING_CONVERSATION_ID,
                user,
                LLM_CONVERSATION_ID,
                null,
                command.context()
        );

        given(userFinder.findOrThrow(USER_ID)).willReturn(user);
        given(coachingLlm.createConversation()).willReturn(LLM_CONVERSATION_ID);
        given(coachingConversationRepository.save(any(CoachingConversation.class)))
                .willReturn(savedCoachingConversation);

        Long coachingConversationId = coachingConversationCommandService.create(command);

        assertThat(coachingConversationId).isEqualTo(COACHING_CONVERSATION_ID);
        then(userFinder).should().findOrThrow(USER_ID);
        then(coachingLlm).should().createConversation();

        ArgumentCaptor<CoachingConversation> coachingConversationCaptor =
                ArgumentCaptor.forClass(CoachingConversation.class);
        then(coachingConversationRepository).should().save(coachingConversationCaptor.capture());

        CoachingConversation coachingConversationToSave = coachingConversationCaptor.getValue();
        assertThat(coachingConversationToSave.getCoachingConversationId()).isNull();
        assertThat(coachingConversationToSave.getUser()).isEqualTo(user);
        assertThat(coachingConversationToSave.getLlmConversationId()).isEqualTo(LLM_CONVERSATION_ID);
        assertThat(coachingConversationToSave.getTitle()).isNull();
        assertThat(coachingConversationToSave.getContext()).isEqualTo(command.context());
    }

    @Test
    void 회원을_조회할_수_없으면_코칭_대화를_생성할_수_없다() {
        CoachingConversationCreateCommand command = createCoachingConversationCreateCommand(UNKNOWN_USER_ID);
        given(userFinder.findOrThrow(UNKNOWN_USER_ID)).willThrow(new ResourceNotFoundException(USER_NOT_FOUND));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> coachingConversationCommandService.create(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userFinder).should().findOrThrow(UNKNOWN_USER_ID);
        then(coachingLlm).shouldHaveNoInteractions();
        then(coachingConversationRepository).shouldHaveNoInteractions();
    }

    @Test
    void LLM_대화를_생성할_수_없으면_코칭_대화를_생성할_수_없다() {
        User user = createReconstructedUser(USER_ID);
        CoachingConversationCreateCommand command = createCoachingConversationCreateCommand(USER_ID);
        given(userFinder.findOrThrow(command.userId())).willReturn(user);
        given(coachingLlm.createConversation())
                .willThrow(new IllegalStateException("LLM conversation creation failed."));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> coachingConversationCommandService.create(command))
                .withMessage("LLM conversation creation failed.");

        then(userFinder).should().findOrThrow(command.userId());
        then(coachingLlm).should().createConversation();
        then(coachingConversationRepository).shouldHaveNoInteractions();
    }
}
