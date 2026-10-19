package net.akive.api.coaching.application.service;

import net.akive.api.coaching.application.provided.dto.CoachingTurnFindQuery;
import net.akive.api.coaching.application.provided.dto.CoachingTurnResult;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.CoachingMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.COACHING_RESPONSE_CONTENT;
import static net.akive.api.coaching.application.service.fixture.CoachingTurnQueryFixture.createCoachingTurnFindQuery;
import static net.akive.api.coaching.domain.MessageRole.ASSISTANT;
import static net.akive.api.coaching.domain.MessageRole.USER;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.reconstructCoachingConversation;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CoachingTurnQueryServiceTest {

    @Mock
    CoachingConversationRepository coachingConversationRepository;

    @InjectMocks
    CoachingTurnQueryService coachingTurnQueryService;

    @Test
    void 회원_메시지와_AI_코치_응답_메시지를_조회한다() {
        CoachingTurnFindQuery query = createCoachingTurnFindQuery();
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        CoachingMessage userMessage = reconstructUserMessage(coachingConversation);
        CoachingMessage assistantMessage = reconstructAssistantMessage(
                coachingConversation,
                COACHING_RESPONSE_CONTENT
        );
        givenMessage(query.userMessageId(), query, userMessage);
        givenMessage(query.assistantMessageId(), query, assistantMessage);

        Optional<CoachingTurnResult> result = coachingTurnQueryService.find(query);

        assertThat(result).contains(new CoachingTurnResult(MESSAGE_CONTENT, COACHING_RESPONSE_CONTENT));

        InOrder messageFindOrder = inOrder(coachingConversationRepository);
        messageFindOrder.verify(coachingConversationRepository)
                .findCoachingMessageByCoachingMessageIdAndCoachingConversationIdAndUserId(
                        query.userMessageId(),
                        query.coachingConversationId(),
                        query.userId()
                );
        messageFindOrder.verify(coachingConversationRepository)
                .findCoachingMessageByCoachingMessageIdAndCoachingConversationIdAndUserId(
                        query.assistantMessageId(),
                        query.coachingConversationId(),
                        query.userId()
                );
    }

    @Test
    void 회원_메시지를_조회할_수_없으면_회원_메시지와_AI_코치_응답_메시지를_반환하지_않는다() {
        CoachingTurnFindQuery query = createCoachingTurnFindQuery();
        given(coachingConversationRepository.findCoachingMessageByCoachingMessageIdAndCoachingConversationIdAndUserId(
                query.userMessageId(),
                query.coachingConversationId(),
                query.userId()
        )).willReturn(Optional.empty());

        Optional<CoachingTurnResult> result = coachingTurnQueryService.find(query);

        assertThat(result).isEmpty();
        then(coachingConversationRepository).should(never())
                .findCoachingMessageByCoachingMessageIdAndCoachingConversationIdAndUserId(
                        query.assistantMessageId(),
                        query.coachingConversationId(),
                        query.userId()
                );
    }

    @Test
    void 회원_메시지의_역할이_일치하지_않으면_회원_메시지와_AI_코치_응답_메시지를_반환하지_않는다() {
        CoachingTurnFindQuery query = createCoachingTurnFindQuery();
        CoachingMessage assistantMessage = reconstructCoachingMessage(
                query.userMessageId(),
                reconstructCoachingConversation(),
                ASSISTANT,
                COACHING_RESPONSE_CONTENT,
                MESSAGE_CREATED_AT
        );
        givenMessage(query.userMessageId(), query, assistantMessage);

        Optional<CoachingTurnResult> result = coachingTurnQueryService.find(query);

        assertThat(result).isEmpty();
        then(coachingConversationRepository).should(never())
                .findCoachingMessageByCoachingMessageIdAndCoachingConversationIdAndUserId(
                        query.assistantMessageId(),
                        query.coachingConversationId(),
                        query.userId()
                );
    }

    @Test
    void AI_코치_응답_메시지를_조회할_수_없으면_회원_메시지와_AI_코치_응답_메시지를_반환하지_않는다() {
        CoachingTurnFindQuery query = createCoachingTurnFindQuery();
        CoachingMessage userMessage = reconstructUserMessage(reconstructCoachingConversation());
        givenMessage(query.userMessageId(), query, userMessage);
        given(coachingConversationRepository.findCoachingMessageByCoachingMessageIdAndCoachingConversationIdAndUserId(
                query.assistantMessageId(),
                query.coachingConversationId(),
                query.userId()
        )).willReturn(Optional.empty());

        Optional<CoachingTurnResult> result = coachingTurnQueryService.find(query);

        assertThat(result).isEmpty();
    }

    @Test
    void AI_코치_응답_메시지의_역할이_일치하지_않으면_회원_메시지와_AI_코치_응답_메시지를_반환하지_않는다() {
        CoachingTurnFindQuery query = createCoachingTurnFindQuery();
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        CoachingMessage userMessage = reconstructUserMessage(coachingConversation);
        CoachingMessage anotherUserMessage = reconstructCoachingMessage(
                query.assistantMessageId(),
                coachingConversation,
                USER,
                "다른 회원 메시지입니다.",
                MESSAGE_CREATED_AT
        );
        givenMessage(query.userMessageId(), query, userMessage);
        givenMessage(query.assistantMessageId(), query, anotherUserMessage);

        Optional<CoachingTurnResult> result = coachingTurnQueryService.find(query);

        assertThat(result).isEmpty();
    }

    private void givenMessage(
            Long coachingMessageId,
            CoachingTurnFindQuery query,
            CoachingMessage coachingMessage
    ) {
        given(coachingConversationRepository.findCoachingMessageByCoachingMessageIdAndCoachingConversationIdAndUserId(
                coachingMessageId,
                query.coachingConversationId(),
                query.userId()
        )).willReturn(Optional.of(coachingMessage));
    }

}
