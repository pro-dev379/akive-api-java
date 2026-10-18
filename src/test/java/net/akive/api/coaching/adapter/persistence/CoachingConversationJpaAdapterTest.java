package net.akive.api.coaching.adapter.persistence;

import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.application.required.data.CoachingConversationSummaryData;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.CoachingMessage;
import net.akive.api.coaching.domain.ConversationContext;
import net.akive.api.common.pagination.CursorCondition;
import net.akive.api.common.pagination.CursorResult;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.COACHING_RESPONSE_CONTENT;
import static net.akive.api.coaching.domain.ConversationContext.MAIN;
import static net.akive.api.coaching.domain.ConversationContext.REPORT;
import static net.akive.api.coaching.domain.MessageCompletionStatus.COMPLETED;
import static net.akive.api.coaching.domain.MessageRole.ASSISTANT;
import static net.akive.api.coaching.domain.MessageRole.USER;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.*;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.MESSAGE_CONTENT;
import static net.akive.api.common.pagination.fixture.PaginationFixture.CURSOR_PAGE_SIZE;
import static net.akive.api.user.domain.fixture.UserFixture.ANOTHER_EMAIL;
import static net.akive.api.user.domain.fixture.UserFixture.ANOTHER_NICKNAME;
import static org.assertj.core.api.Assertions.assertThat;

class CoachingConversationJpaAdapterTest extends RepositoryTestSupport {

    private static final String REPORT_LLM_CONVERSATION_ID = "conv_report";
    private static final String ANOTHER_USER_LLM_CONVERSATION_ID = "conv_other_user";
    private static final String REPORT_CONVERSATION_TITLE = "리포트 코칭";

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

    @Test
    void 회원_식별자와_코칭_대화_컨텍스트로_코칭_대화_목록을_조회한다() {
        User savedUser = saveUser();
        CoachingConversation expectedConversation = saveCoachingConversation(
                savedUser,
                LLM_CONVERSATION_ID,
                MAIN,
                CONVERSATION_TITLE
        );
        CoachingConversation reportConversation = saveCoachingConversation(
                savedUser,
                REPORT_LLM_CONVERSATION_ID,
                REPORT,
                REPORT_CONVERSATION_TITLE
        );
        CoachingConversation anotherUserConversation = saveCoachingConversation(
                saveUser(ANOTHER_EMAIL, ANOTHER_NICKNAME),
                ANOTHER_USER_LLM_CONVERSATION_ID,
                MAIN,
                CONVERSATION_TITLE
        );
        coachingConversationRepository.saveMessage(
                CoachingMessage.fromUser(expectedConversation, MESSAGE_CONTENT)
        );
        coachingConversationRepository.saveMessage(
                CoachingMessage.fromUser(reportConversation, MESSAGE_CONTENT)
        );
        coachingConversationRepository.saveMessage(
                CoachingMessage.fromUser(anotherUserConversation, MESSAGE_CONTENT)
        );
        flushAndClear();

        CursorResult<CoachingConversationSummaryData, Long> summaryPage = coachingConversationRepository
                .findSummaryPageByUserIdAndContext(
                        savedUser.getUserId(),
                        MAIN,
                        CursorCondition.of(null, CURSOR_PAGE_SIZE)
                );

        assertThat(summaryPage.contents())
                .extracting(CoachingConversationSummaryData::coachingConversationId)
                .containsExactly(expectedConversation.getCoachingConversationId());
    }

    @Test
    void 코칭_대화_목록을_최신_코칭_메시지_순으로_커서_페이징_조회한다() {
        User savedUser = saveUser();
        CoachingConversation olderConversation = saveCoachingConversation(
                savedUser,
                LLM_CONVERSATION_ID,
                MAIN,
                CONVERSATION_TITLE
        );
        CoachingConversation newerConversation = saveCoachingConversation(
                savedUser,
                ANOTHER_LLM_CONVERSATION_ID,
                MAIN,
                CONVERSATION_TITLE
        );
        CoachingMessage olderLatestMessage = coachingConversationRepository.saveMessage(
                CoachingMessage.fromUser(olderConversation, MESSAGE_CONTENT)
        );
        CoachingMessage cursorMessage = coachingConversationRepository.saveMessage(
                CoachingMessage.fromUser(newerConversation, MESSAGE_CONTENT)
        );
        flushAndClear();

        CursorResult<CoachingConversationSummaryData, Long> firstPage = coachingConversationRepository
                .findSummaryPageByUserIdAndContext(
                        savedUser.getUserId(),
                        MAIN,
                        CursorCondition.of(null, 1)
                );
        CursorResult<CoachingConversationSummaryData, Long> secondPage = coachingConversationRepository
                .findSummaryPageByUserIdAndContext(
                        savedUser.getUserId(),
                        MAIN,
                        CursorCondition.of(firstPage.cursorInfo().nextCursor(), 1)
                );

        assertThat(firstPage.contents())
                .extracting(CoachingConversationSummaryData::coachingConversationId)
                .containsExactly(newerConversation.getCoachingConversationId());
        assertThat(firstPage.cursorInfo().hasNext()).isTrue();
        assertThat(firstPage.cursorInfo().nextCursor()).isEqualTo(cursorMessage.getCoachingMessageId());
        assertThat(secondPage.contents())
                .extracting(CoachingConversationSummaryData::coachingConversationId)
                .containsExactly(olderConversation.getCoachingConversationId());
        assertThat(secondPage.contents().getFirst().latestMessageId())
                .isEqualTo(olderLatestMessage.getCoachingMessageId());
        assertThat(secondPage.cursorInfo().hasNext()).isFalse();
        assertThat(secondPage.cursorInfo().nextCursor()).isNull();
    }

    @Test
    void 코칭_대화_목록_조회_결과에_코칭_대화_정보와_최신_메시지_정보를_포함한다() {
        User savedUser = saveUser();
        CoachingConversation savedConversation = saveCoachingConversation(
                savedUser,
                LLM_CONVERSATION_ID,
                MAIN,
                CONVERSATION_TITLE
        );
        coachingConversationRepository.saveMessage(CoachingMessage.fromUser(savedConversation, MESSAGE_CONTENT));
        coachingConversationRepository.saveMessage(
                CoachingMessage.fromAssistant(savedConversation, COACHING_RESPONSE_CONTENT, COMPLETED)
        );
        flushAndClear();

        CoachingConversation persistedConversation = coachingConversationRepository
                .findByCoachingConversationIdAndUserId(
                        savedConversation.getCoachingConversationId(),
                        savedUser.getUserId()
                )
                .orElseThrow();
        CoachingMessage persistedLatestMessage = coachingConversationRepository
                .findLatestMessage(savedConversation.getCoachingConversationId())
                .orElseThrow();
        CursorResult<CoachingConversationSummaryData, Long> summaryPage = coachingConversationRepository
                .findSummaryPageByUserIdAndContext(
                        savedUser.getUserId(),
                        MAIN,
                        CursorCondition.of(null, CURSOR_PAGE_SIZE)
                );

        assertThat(summaryPage.contents()).hasSize(1);
        CoachingConversationSummaryData summary = summaryPage.contents().getFirst();
        assertThat(summary.coachingConversationId()).isEqualTo(savedConversation.getCoachingConversationId());
        assertThat(summary.title()).isEqualTo(CONVERSATION_TITLE);
        assertThat(summary.context()).isEqualTo(MAIN);
        assertThat(summary.preview()).isEqualTo(COACHING_RESPONSE_CONTENT);
        assertThat(summary.lastMessageCreatedAt()).isEqualTo(persistedLatestMessage.getCreatedAt());
        assertThat(summary.createdAt()).isEqualTo(persistedConversation.getCreatedAt());
        assertThat(summary.updatedAt()).isNotNull().isAfterOrEqualTo(summary.createdAt());
        assertThat(summary.latestMessageId()).isEqualTo(persistedLatestMessage.getCoachingMessageId());
    }

    @Test
    void 코칭_메시지가_없는_코칭_대화는_코칭_대화_목록에서_조회되지_않는다() {
        User savedUser = saveUser();
        saveCoachingConversation(savedUser, LLM_CONVERSATION_ID, MAIN, CONVERSATION_TITLE);
        flushAndClear();

        CursorResult<CoachingConversationSummaryData, Long> summaryPage = coachingConversationRepository
                .findSummaryPageByUserIdAndContext(
                        savedUser.getUserId(),
                        MAIN,
                        CursorCondition.of(null, CURSOR_PAGE_SIZE)
                );

        assertThat(summaryPage.contents()).isEmpty();
        assertThat(summaryPage.cursorInfo().hasNext()).isFalse();
        assertThat(summaryPage.cursorInfo().nextCursor()).isNull();
    }

    private CoachingConversation saveCoachingConversation() {
        User savedUser = saveUser();

        return coachingConversationRepository.save(
                createCoachingConversation(savedUser, LLM_CONVERSATION_ID, MAIN)
        );
    }

    private CoachingConversation saveCoachingConversation(
            User user,
            String llmConversationId,
            ConversationContext context,
            String title
    ) {
        CoachingConversation savedConversation = coachingConversationRepository.save(
                createCoachingConversation(user, llmConversationId, context)
        );
        savedConversation.updateTitle(title);

        return coachingConversationRepository.save(savedConversation);
    }
}
