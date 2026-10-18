package net.akive.api.coaching.application.service;

import net.akive.api.coaching.application.provided.dto.CoachingConversationSummaryResult;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.application.required.data.CoachingConversationSummaryData;
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

import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.COACHING_RESPONSE_CONTENT;
import static net.akive.api.coaching.domain.ConversationContext.MAIN;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.*;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.COACHING_MESSAGE_ID;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.MESSAGE_CREATED_AT;
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

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void 커서가_양수가_아니면_예외를_던진다(long cursor) {
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
}
