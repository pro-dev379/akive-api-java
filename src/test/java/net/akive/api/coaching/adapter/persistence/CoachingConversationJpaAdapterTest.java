package net.akive.api.coaching.adapter.persistence;

import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.CoachingMessage;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.COACHING_RESPONSE_CONTENT;
import static net.akive.api.coaching.domain.ConversationContext.MAIN;
import static net.akive.api.coaching.domain.MessageCompletionStatus.COMPLETED;
import static net.akive.api.coaching.domain.MessageRole.ASSISTANT;
import static net.akive.api.coaching.domain.MessageRole.USER;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.*;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.MESSAGE_CONTENT;
import static org.assertj.core.api.Assertions.assertThat;

class CoachingConversationJpaAdapterTest extends RepositoryTestSupport {

    @Autowired
    CoachingConversationRepository coachingConversationRepository;

    @Test
    void 코칭_대화를_저장한다() {
        User savedUser = saveUser();
        CoachingConversation coachingConversation = createCoachingConversation(savedUser, LLM_CONVERSATION_ID, MAIN);

        CoachingConversation savedCoachingConversation = coachingConversationRepository.save(coachingConversation);
        flushAndClear();

        CoachingConversation foundCoachingConversation = coachingConversationRepository
                .findByCoachingConversationIdAndUserId(
                        savedCoachingConversation.getCoachingConversationId(),
                        savedUser.getUserId()
                )
                .orElseThrow();

        assertThat(foundCoachingConversation.getCoachingConversationId())
                .isEqualTo(savedCoachingConversation.getCoachingConversationId());
        assertThat(foundCoachingConversation.getUser().getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(foundCoachingConversation.getLlmConversationId()).isEqualTo(LLM_CONVERSATION_ID);
        assertThat(foundCoachingConversation.getTitle()).isNull();
        assertThat(foundCoachingConversation.getContext()).isEqualTo(MAIN);
    }

    @Test
    void 코칭_대화_제목을_수정한다() {
        CoachingConversation savedCoachingConversation = saveCoachingConversation();
        savedCoachingConversation.updateTitle(CONVERSATION_TITLE);

        coachingConversationRepository.save(savedCoachingConversation);
        flushAndClear();

        CoachingConversation foundCoachingConversation = coachingConversationRepository
                .findByCoachingConversationIdAndUserId(
                        savedCoachingConversation.getCoachingConversationId(),
                        savedCoachingConversation.getUser().getUserId()
                )
                .orElseThrow();

        assertThat(foundCoachingConversation.getTitle()).isEqualTo(CONVERSATION_TITLE);
    }

    @Test
    void 코칭_대화_식별자와_회원_식별자로_코칭_대화를_조회한다() {
        CoachingConversation savedCoachingConversation = saveCoachingConversation();
        flushAndClear();

        Optional<CoachingConversation> foundCoachingConversation =
                coachingConversationRepository.findByCoachingConversationIdAndUserId(
                        savedCoachingConversation.getCoachingConversationId(),
                        savedCoachingConversation.getUser().getUserId()
                );

        assertThat(foundCoachingConversation).contains(savedCoachingConversation);
    }

    @Test
    void 코칭_메시지를_저장한다() {
        CoachingConversation savedCoachingConversation = saveCoachingConversation();
        CoachingMessage userMessage = CoachingMessage.fromUser(savedCoachingConversation, MESSAGE_CONTENT);

        CoachingMessage savedUserMessage = coachingConversationRepository.saveMessage(userMessage);
        flushAndClear();

        CoachingMessage foundUserMessage = coachingConversationRepository
                .findMessageByCoachingMessageIdAndCoachingConversationIdAndUserId(
                        savedUserMessage.getCoachingMessageId(),
                        savedCoachingConversation.getCoachingConversationId(),
                        savedCoachingConversation.getUser().getUserId()
                )
                .orElseThrow();
        assertThat(foundUserMessage.getCoachingMessageId()).isEqualTo(savedUserMessage.getCoachingMessageId());
        assertThat(foundUserMessage.getCoachingConversation()).isEqualTo(savedCoachingConversation);
        assertThat(foundUserMessage.getRole()).isEqualTo(USER);
        assertThat(foundUserMessage.getContent()).isEqualTo(MESSAGE_CONTENT);
        assertThat(foundUserMessage.getCompletionStatus()).isEqualTo(COMPLETED);
        assertThat(foundUserMessage.getCreatedAt()).isNotNull();
    }

    @Test
    void 코칭_메시지와_코칭_대화와_회원_식별자로_코칭_메시지를_조회한다() {
        CoachingConversation savedCoachingConversation = saveCoachingConversation();
        CoachingMessage savedUserMessage = coachingConversationRepository.saveMessage(
                CoachingMessage.fromUser(savedCoachingConversation, MESSAGE_CONTENT)
        );
        flushAndClear();

        Optional<CoachingMessage> foundUserMessage = coachingConversationRepository
                .findMessageByCoachingMessageIdAndCoachingConversationIdAndUserId(
                        savedUserMessage.getCoachingMessageId(),
                        savedCoachingConversation.getCoachingConversationId(),
                        savedCoachingConversation.getUser().getUserId()
                );

        assertThat(foundUserMessage).contains(savedUserMessage);
    }

    @Test
    void 가장_최근에_저장된_코칭_메시지를_조회한다() {
        CoachingConversation savedCoachingConversation = saveCoachingConversation();
        coachingConversationRepository.saveMessage(
                CoachingMessage.fromUser(savedCoachingConversation, MESSAGE_CONTENT)
        );
        CoachingMessage savedAssistantMessage = coachingConversationRepository.saveMessage(
                CoachingMessage.fromAssistant(savedCoachingConversation, COACHING_RESPONSE_CONTENT, COMPLETED)
        );
        flushAndClear();

        Optional<CoachingMessage> latestMessage = coachingConversationRepository.findLatestMessage(
                savedCoachingConversation.getCoachingConversationId()
        );

        assertThat(latestMessage).contains(savedAssistantMessage);
        assertThat(latestMessage.get().getRole()).isEqualTo(ASSISTANT);
        assertThat(latestMessage.get().getContent()).isEqualTo(COACHING_RESPONSE_CONTENT);
    }

    private CoachingConversation saveCoachingConversation() {
        User savedUser = saveUser();

        return coachingConversationRepository.save(
                createCoachingConversation(savedUser, LLM_CONVERSATION_ID, MAIN)
        );
    }
}
