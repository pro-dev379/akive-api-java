package net.akive.api.coaching.adapter.webapi;

import net.akive.api.coaching.adapter.webapi.request.CoachingConversationCreateRequest;
import net.akive.api.coaching.application.provided.CoachingConversationCreator;
import net.akive.api.coaching.application.provided.dto.CoachingConversationCreateCommand;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import static net.akive.api.coaching.domain.ConversationContext.MAIN;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.COACHING_CONVERSATION_ID;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.fixture.UserFixture.UNKNOWN_USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@WebMvcTest(CoachingApi.class)
class CoachingApiTest extends PrivateApiTestSupport {

    private static final String COACHING_CONVERSATIONS_URL = "/coaching/conversations";

    @MockitoBean
    CoachingConversationCreator coachingConversationCreator;

    @Test
    void 코칭_대화를_생성한다() {
        CoachingConversationCreateRequest request = new CoachingConversationCreateRequest(MAIN);
        CoachingConversationCreateCommand command = request.to(USER_ID);
        given(coachingConversationCreator.create(command)).willReturn(COACHING_CONVERSATION_ID);

        MvcTestResultAssert result = postJson(
                COACHING_CONVERSATIONS_URL,
                USER_ID,
                toJson(request)
        ).hasStatus(CREATED);

        result.bodyJson()
                .extractingPath("$.coachingConversationId")
                .isEqualTo(COACHING_CONVERSATION_ID.intValue());

        then(coachingConversationCreator).should().create(command);
    }

    @Test
    void 코칭_대화_생성_시_대화_컨텍스트가_없으면_400을_응답한다() {
        CoachingConversationCreateRequest request = new CoachingConversationCreateRequest(null);

        postJson(COACHING_CONVERSATIONS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(coachingConversationCreator).shouldHaveNoInteractions();
    }

    @Test
    void 코칭_대화_생성_시_회원이_없으면_404를_응답한다() {
        CoachingConversationCreateRequest request = new CoachingConversationCreateRequest(MAIN);
        CoachingConversationCreateCommand command = request.to(UNKNOWN_USER_ID);
        willThrow(new ResourceNotFoundException(USER_NOT_FOUND))
                .given(coachingConversationCreator)
                .create(command);

        MvcTestResultAssert result = postJson(
                COACHING_CONVERSATIONS_URL,
                UNKNOWN_USER_ID,
                toJson(request)
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);
        then(coachingConversationCreator).should().create(command);
    }
}
