package net.akive.api.coaching.application.service;

import net.akive.api.coaching.application.event.CoachingTurnCompletedEvent;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.application.required.data.CoachingTurnRecoveryData;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.CoachingMessage;
import net.akive.api.eventprocessing.application.provided.IntegrationEventRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.COACHING_RESPONSE_CONTENT;
import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.EVENT_ID;
import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.createCoachingTurnRecoveryData;
import static net.akive.api.coaching.domain.MessageCompletionStatus.TRUNCATED;
import static net.akive.api.coaching.domain.MessageRole.ASSISTANT;
import static net.akive.api.coaching.domain.MessageRole.USER;
import static net.akive.api.coaching.domain.ConversationContext.MAIN;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.*;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.*;
import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CoachingTurnCompletionServiceTest {

    @Mock
    CoachingConversationRepository coachingConversationRepository;

    @Mock
    IntegrationEventRecorder integrationEventRecorder;

    @InjectMocks
    CoachingTurnCompletionService coachingTurnCompletionService;

    @Test
    void 완료된_AI_코치_응답_메시지를_저장하고_코칭_메시지_교환_완료_이벤트를_기록한다() {
        CoachingConversation coachingConversation = createPersistedCoachingConversation();
        CoachingTurnRecoveryData recoveryData = createCoachingTurnRecoveryData();
        CoachingMessage savedAssistantMessage = reconstructAssistantMessage(
                coachingConversation,
                recoveryData.content(),
                recoveryData.completionStatus()
        );
        given(coachingConversationRepository.findLatestMessage(coachingConversation.getCoachingConversationId()))
                .willReturn(Optional.empty());
        given(coachingConversationRepository.saveMessage(any(CoachingMessage.class)))
                .willReturn(savedAssistantMessage);

        coachingTurnCompletionService.complete(coachingConversation, recoveryData);

        ArgumentCaptor<CoachingMessage> assistantMessageCaptor = ArgumentCaptor.forClass(CoachingMessage.class);
        InOrder completionOrder = inOrder(coachingConversationRepository, integrationEventRecorder);
        completionOrder.verify(coachingConversationRepository).saveMessage(assistantMessageCaptor.capture());

        CoachingMessage assistantMessageToSave = assistantMessageCaptor.getValue();
        assertThat(assistantMessageToSave.getCoachingConversation()).isEqualTo(coachingConversation);
        assertThat(assistantMessageToSave.getRole()).isEqualTo(ASSISTANT);
        assertThat(assistantMessageToSave.getContent()).isEqualTo(recoveryData.content());
        assertThat(assistantMessageToSave.getCompletionStatus()).isEqualTo(recoveryData.completionStatus());

        ArgumentCaptor<CoachingTurnCompletedEvent> completedEventCaptor =
                ArgumentCaptor.forClass(CoachingTurnCompletedEvent.class);
        completionOrder.verify(integrationEventRecorder).record(completedEventCaptor.capture());

        CoachingTurnCompletedEvent completedEvent = completedEventCaptor.getValue();
        assertThat(completedEvent.eventId()).isEqualTo(EVENT_ID);
        assertThat(completedEvent.eventType()).isEqualTo(COACHING_TURN_DONE);
        assertThat(completedEvent.userId()).isEqualTo(coachingConversation.getUser().getUserId());
        assertThat(completedEvent.coachingConversationId())
                .isEqualTo(coachingConversation.getCoachingConversationId());
        assertThat(completedEvent.userMessageId()).isEqualTo(recoveryData.userMessageId());
        assertThat(completedEvent.assistantMessageId())
                .isEqualTo(savedAssistantMessage.getCoachingMessageId());
        assertThat(completedEvent.completedAt()).isEqualTo(savedAssistantMessage.getCreatedAt());
    }

    @Test
    void 동일한_AI_코치_응답_메시지가_이미_저장되어_있으면_중복_저장하지_않는다() {
        CoachingConversation coachingConversation = createPersistedCoachingConversation();
        CoachingTurnRecoveryData recoveryData = createCoachingTurnRecoveryData();
        CoachingMessage savedAssistantMessage = reconstructAssistantMessage(
                coachingConversation,
                recoveryData.content(),
                recoveryData.completionStatus()
        );
        given(coachingConversationRepository.findLatestMessage(coachingConversation.getCoachingConversationId()))
                .willReturn(Optional.of(savedAssistantMessage));

        coachingTurnCompletionService.complete(coachingConversation, recoveryData);

        then(coachingConversationRepository).should(never()).saveMessage(any(CoachingMessage.class));

        ArgumentCaptor<CoachingTurnCompletedEvent> completedEventCaptor =
                ArgumentCaptor.forClass(CoachingTurnCompletedEvent.class);
        then(integrationEventRecorder).should().record(completedEventCaptor.capture());
        assertThat(completedEventCaptor.getValue().assistantMessageId())
                .isEqualTo(savedAssistantMessage.getCoachingMessageId());
    }

    @Test
    void 가장_최근_메시지가_회원_메시지이면_AI_코치_응답_메시지를_저장한다() {
        CoachingConversation coachingConversation = createPersistedCoachingConversation();
        CoachingTurnRecoveryData recoveryData = createCoachingTurnRecoveryData();
        CoachingMessage latestUserMessage = reconstructCoachingMessage(
                COACHING_MESSAGE_ID,
                coachingConversation,
                USER,
                recoveryData.content(),
                MESSAGE_CREATED_AT
        );

        assertSavesAssistantMessage(coachingConversation, recoveryData, latestUserMessage);
    }

    @Test
    void 가장_최근_AI_코치_응답_내용이_다르면_새_응답_메시지를_저장한다() {
        CoachingConversation coachingConversation = createPersistedCoachingConversation();
        CoachingTurnRecoveryData recoveryData = createCoachingTurnRecoveryData();
        CoachingMessage latestAssistantMessage = reconstructCoachingMessage(
                ANOTHER_COACHING_MESSAGE_ID,
                coachingConversation,
                ASSISTANT,
                "이전에 생성된 다른 응답입니다.",
                MESSAGE_CREATED_AT
        );

        assertSavesAssistantMessage(coachingConversation, recoveryData, latestAssistantMessage);
    }

    @Test
    void 가장_최근_AI_코치_응답_완료_상태가_다르면_새_응답_메시지를_저장한다() {
        CoachingConversation coachingConversation = createPersistedCoachingConversation();
        CoachingTurnRecoveryData recoveryData = createCoachingTurnRecoveryData();
        CoachingMessage latestAssistantMessage = reconstructCoachingMessage(
                ANOTHER_COACHING_MESSAGE_ID,
                coachingConversation,
                ASSISTANT,
                COACHING_RESPONSE_CONTENT,
                TRUNCATED,
                MESSAGE_CREATED_AT
        );

        assertSavesAssistantMessage(coachingConversation, recoveryData, latestAssistantMessage);
    }

    private void assertSavesAssistantMessage(
            CoachingConversation coachingConversation,
            CoachingTurnRecoveryData recoveryData,
            CoachingMessage latestMessage
    ) {
        CoachingMessage savedAssistantMessage = reconstructAssistantMessage(
                coachingConversation,
                recoveryData.content(),
                recoveryData.completionStatus()
        );
        given(coachingConversationRepository.findLatestMessage(coachingConversation.getCoachingConversationId()))
                .willReturn(Optional.of(latestMessage));
        given(coachingConversationRepository.saveMessage(any(CoachingMessage.class)))
                .willReturn(savedAssistantMessage);

        coachingTurnCompletionService.complete(coachingConversation, recoveryData);

        ArgumentCaptor<CoachingMessage> assistantMessageCaptor = ArgumentCaptor.forClass(CoachingMessage.class);
        then(coachingConversationRepository).should().saveMessage(assistantMessageCaptor.capture());
        assertThat(assistantMessageCaptor.getValue().getRole()).isEqualTo(ASSISTANT);
        assertThat(assistantMessageCaptor.getValue().getContent()).isEqualTo(recoveryData.content());
        assertThat(assistantMessageCaptor.getValue().getCompletionStatus())
                .isEqualTo(recoveryData.completionStatus());
        then(integrationEventRecorder).should().record(any(CoachingTurnCompletedEvent.class));
    }

    private CoachingConversation createPersistedCoachingConversation() {
        return reconstructCoachingConversation(
                COACHING_CONVERSATION_ID,
                createReconstructedUser(),
                LLM_CONVERSATION_ID,
                CONVERSATION_TITLE,
                MAIN
        );
    }
}
