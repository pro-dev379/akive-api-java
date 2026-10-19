package net.akive.api.coaching.application.service;

import net.akive.api.coaching.application.provided.dto.CoachingConversationSummaryResult;
import net.akive.api.coaching.application.provided.dto.CoachingMessageResult;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.application.required.data.CoachingConversationSummaryData;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.CoachingMessage;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.pagination.CursorCondition;
import net.akive.api.common.pagination.CursorResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.COACHING_RESPONSE_CONTENT;
import static net.akive.api.coaching.domain.ConversationContext.MAIN;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.*;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.COACHING_MESSAGE_ID;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.MESSAGE_CREATED_AT;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.reconstructCoachingMessage;
import static net.akive.api.coaching.domain.MessageCompletionStatus.TRUNCATED;
import static net.akive.api.coaching.domain.MessageRole.ASSISTANT;
import static net.akive.api.common.exception.business.ResourceNotFoundError.COACHING_CONVERSATION_NOT_FOUND;
import static net.akive.api.common.pagination.fixture.PaginationFixture.CURSOR_PAGE_SIZE;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CoachingConversationQueryServiceTest {

    @Mock
    CoachingConversationRepository coachingConversationRepository;

    @InjectMocks
    CoachingConversationQueryService coachingConversationQueryService;

    @Test
    void 회원_식별자와_코칭_대화_컨텍스트로_코칭_대화_목록을_커서_페이징_조회한다() {
        CursorCondition<Long> cursorCondition = CursorCondition.of(null, CURSOR_PAGE_SIZE);
        CoachingConversationSummaryData summaryData = new CoachingConversationSummaryData(
                COACHING_CONVERSATION_ID,
                CONVERSATION_TITLE,
                MAIN,
                COACHING_RESPONSE_CONTENT,
                MESSAGE_CREATED_AT,
                CREATED_AT,
                UPDATED_AT,
                COACHING_MESSAGE_ID
        );
        CursorResult<CoachingConversationSummaryData, Long> summaryPage = CursorResult.of(
                COACHING_MESSAGE_ID,
                true,
                List.of(summaryData)
        );
        given(coachingConversationRepository.findSummaryPageByUserIdAndContext(
                USER_ID,
                MAIN,
                cursorCondition
        )).willReturn(summaryPage);

        CursorResult<CoachingConversationSummaryResult, Long> result =
                coachingConversationQueryService.findSummaryPage(USER_ID, MAIN, cursorCondition);

        assertThat(result.cursorInfo()).isEqualTo(summaryPage.cursorInfo());
        assertThat(result.contents()).containsExactly(new CoachingConversationSummaryResult(
                COACHING_CONVERSATION_ID,
                CONVERSATION_TITLE,
                MAIN,
                COACHING_RESPONSE_CONTENT,
                MESSAGE_CREATED_AT,
                CREATED_AT,
                UPDATED_AT
        ));
        then(coachingConversationRepository).should().findSummaryPageByUserIdAndContext(
                USER_ID,
                MAIN,
                cursorCondition
        );
    }

    @Test
    void 코칭_대화의_메시지_목록을_커서_페이징_조회한다() {
        CursorCondition<Long> cursorCondition = CursorCondition.of(null, CURSOR_PAGE_SIZE);
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        CoachingMessage coachingMessage = reconstructCoachingMessage(
                COACHING_MESSAGE_ID,
                coachingConversation,
                ASSISTANT,
                COACHING_RESPONSE_CONTENT,
                TRUNCATED,
                MESSAGE_CREATED_AT
        );
        CursorResult<CoachingMessage, Long> messagePage = CursorResult.of(
                COACHING_MESSAGE_ID,
                true,
                List.of(coachingMessage)
        );
        given(coachingConversationRepository.findByCoachingConversationIdAndUserId(
                COACHING_CONVERSATION_ID,
                USER_ID
        )).willReturn(Optional.of(coachingConversation));
        given(coachingConversationRepository.findCoachingMessagePageByCoachingConversationId(
                COACHING_CONVERSATION_ID,
                cursorCondition
        )).willReturn(messagePage);

        CursorResult<CoachingMessageResult, Long> result = coachingConversationQueryService.findMessagePage(
                USER_ID,
                COACHING_CONVERSATION_ID,
                cursorCondition
        );

        assertThat(result.cursorInfo()).isEqualTo(messagePage.cursorInfo());
        assertThat(result.contents()).containsExactly(new CoachingMessageResult(
                COACHING_MESSAGE_ID,
                ASSISTANT,
                COACHING_RESPONSE_CONTENT,
                TRUNCATED,
                MESSAGE_CREATED_AT
        ));
        then(coachingConversationRepository).should().findByCoachingConversationIdAndUserId(
                COACHING_CONVERSATION_ID,
                USER_ID
        );
        then(coachingConversationRepository).should().findCoachingMessagePageByCoachingConversationId(
                COACHING_CONVERSATION_ID,
                cursorCondition
        );
    }

    @Test
    void 코칭_메시지_목록_조회_시_코칭_대화가_없으면_예외를_던진다() {
        CursorCondition<Long> cursorCondition = CursorCondition.of(null, CURSOR_PAGE_SIZE);
        given(coachingConversationRepository.findByCoachingConversationIdAndUserId(
                UNKNOWN_COACHING_CONVERSATION_ID,
                USER_ID
        )).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> coachingConversationQueryService.findMessagePage(
                        USER_ID,
                        UNKNOWN_COACHING_CONVERSATION_ID,
                        cursorCondition
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(COACHING_CONVERSATION_NOT_FOUND);

        then(coachingConversationRepository).should().findByCoachingConversationIdAndUserId(
                UNKNOWN_COACHING_CONVERSATION_ID,
                USER_ID
        );
        then(coachingConversationRepository).shouldHaveNoMoreInteractions();
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void 코칭_대화_목록_조회_시_커서가_양수가_아니면_예외를_던진다(long cursor) {
        CursorCondition<Long> cursorCondition = CursorCondition.of(cursor, CURSOR_PAGE_SIZE);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> coachingConversationQueryService.findSummaryPage(
                        USER_ID,
                        MAIN,
                        cursorCondition
                ))
                .withMessage("Cursor must be positive.");

        then(coachingConversationRepository).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void 코칭_메시지_목록_조회_시_커서가_양수가_아니면_예외를_던진다(long cursor) {
        CursorCondition<Long> cursorCondition = CursorCondition.of(cursor, CURSOR_PAGE_SIZE);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> coachingConversationQueryService.findMessagePage(
                        USER_ID,
                        COACHING_CONVERSATION_ID,
                        cursorCondition
                ))
                .withMessage("Cursor must be positive.");

        then(coachingConversationRepository).shouldHaveNoInteractions();
    }
}
