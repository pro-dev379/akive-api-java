package net.akive.api.coaching.adapter.webapi;

import net.akive.api.coaching.adapter.webapi.request.CoachingConversationCreateRequest;
import net.akive.api.coaching.adapter.webapi.request.CoachingMessageSendRequest;
import net.akive.api.coaching.application.provided.CoachingConversationCreator;
import net.akive.api.coaching.application.provided.CoachingConversationFinder;
import net.akive.api.coaching.application.provided.CoachingMessageSender;
import net.akive.api.coaching.application.provided.dto.CoachingConversationCreateCommand;
import net.akive.api.coaching.application.provided.dto.CoachingConversationSummaryResult;
import net.akive.api.coaching.application.provided.dto.CoachingMessageSendCommand;
import net.akive.api.coaching.application.provided.dto.CoachingMessageSendResult;
import net.akive.api.coaching.application.provided.dto.CoachingMessageResult;
import net.akive.api.coaching.application.stream.CoachingResponseStream;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.pagination.CursorCondition;
import net.akive.api.common.pagination.CursorResult;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.COACHING_RESPONSE_CONTENT;
import static net.akive.api.coaching.application.stream.CoachingResponseStream.IncompleteReason.MAX_OUTPUT_TOKENS;
import static net.akive.api.coaching.domain.CoachingPolicy.USER_MESSAGE_CONTENT_MAX_LENGTH;
import static net.akive.api.coaching.domain.ConversationContext.MAIN;
import static net.akive.api.coaching.domain.MessageCompletionStatus.COMPLETED;
import static net.akive.api.coaching.domain.MessageRole.ASSISTANT;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.*;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_INPUT_VALUE;
import static net.akive.api.common.exception.business.ResourceConflictError.COACHING_RESPONSE_ALREADY_IN_PROGRESS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.COACHING_CONVERSATION_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.common.pagination.fixture.PaginationFixture.CURSOR_PAGE_SIZE;
import static net.akive.api.user.domain.fixture.UserFixture.UNKNOWN_USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CoachingApi.class)
class CoachingApiTest extends PrivateApiTestSupport {

    private static final String COACHING_CONVERSATIONS_URL = "/coaching/conversations";
    private static final String COACHING_CONVERSATION_URL =
            COACHING_CONVERSATIONS_URL + "/{coachingConversationId}";
    private static final String COACHING_MESSAGES_URL =
            COACHING_CONVERSATIONS_URL + "/" + COACHING_CONVERSATION_ID + "/messages";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CoachingConversationCreator coachingConversationCreator;

    @MockitoBean
    CoachingConversationFinder coachingConversationFinder;

    @MockitoBean
    CoachingMessageSender coachingMessageSender;

    @Test
    void 회원_식별자와_코칭_대화_컨텍스트로_코칭_대화_목록을_커서_페이징_조회한다() {
        CoachingConversationSummaryResult summary = new CoachingConversationSummaryResult(
                COACHING_CONVERSATION_ID,
                CONVERSATION_TITLE,
                MAIN,
                COACHING_RESPONSE_CONTENT,
                MESSAGE_CREATED_AT,
                CREATED_AT,
                UPDATED_AT
        );
        CursorResult<CoachingConversationSummaryResult, Long> summaryPage = CursorResult.of(
                COACHING_MESSAGE_ID,
                true,
                List.of(summary)
        );
        given(coachingConversationFinder.findSummaryPage(eq(USER_ID), eq(MAIN), any()))
                .willReturn(summaryPage);

        MvcTestResultAssert result = get(
                COACHING_CONVERSATIONS_URL + "?context={context}&cursor={cursor}&size={size}",
                USER_ID,
                MAIN,
                COACHING_MESSAGE_ID,
                CURSOR_PAGE_SIZE
        ).hasStatusOk();

        assertCoachingConversationsFindResponse(result, summaryPage, summary);

        ArgumentCaptor<CursorCondition<Long>> cursorConditionCaptor = ArgumentCaptor.captor();
        then(coachingConversationFinder).should().findSummaryPage(
                eq(USER_ID),
                eq(MAIN),
                cursorConditionCaptor.capture()
        );
        assertThat(cursorConditionCaptor.getValue().cursor()).isEqualTo(COACHING_MESSAGE_ID);
        assertThat(cursorConditionCaptor.getValue().size()).isEqualTo(CURSOR_PAGE_SIZE);
    }

    @Test
    void 코칭_대화_컨텍스트가_없으면_400을_응답한다() {
        get(COACHING_CONVERSATIONS_URL, USER_ID).hasStatus(BAD_REQUEST);

        then(coachingConversationFinder).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @CsvSource({
            "0, Size must be positive.",
            "101, Size must not exceed 100."
    })
    void 코칭_대화_목록_조회_시_커서_페이징_요청_파라미터가_올바르지_않으면_400을_응답한다(int size, String message) {
        MvcTestResultAssert result = get(
                COACHING_CONVERSATIONS_URL + "?context={context}&size={size}",
                USER_ID,
                MAIN,
                size
        ).hasStatus(BAD_REQUEST);

        assertInvalidCursorConditionResponse(result, message);

        then(coachingConversationFinder).shouldHaveNoInteractions();
    }

    @Test
    void 코칭_대화의_메시지_목록을_커서_페이징_조회한다() {
        CoachingMessageResult message = new CoachingMessageResult(
                COACHING_MESSAGE_ID,
                ASSISTANT,
                COACHING_RESPONSE_CONTENT,
                COMPLETED,
                MESSAGE_CREATED_AT
        );
        CursorResult<CoachingMessageResult, Long> messagePage = CursorResult.of(
                COACHING_MESSAGE_ID,
                true,
                List.of(message)
        );
        given(coachingConversationFinder.findMessagePage(
                eq(USER_ID),
                eq(COACHING_CONVERSATION_ID),
                any()
        )).willReturn(messagePage);

        MvcTestResultAssert result = get(
                COACHING_MESSAGES_URL + "?cursor={cursor}&size={size}",
                USER_ID,
                COACHING_MESSAGE_ID,
                CURSOR_PAGE_SIZE
        ).hasStatusOk();

        assertCoachingMessagesFindResponse(result, messagePage, message);

        ArgumentCaptor<CursorCondition<Long>> cursorConditionCaptor = ArgumentCaptor.captor();
        then(coachingConversationFinder).should().findMessagePage(
                eq(USER_ID),
                eq(COACHING_CONVERSATION_ID),
                cursorConditionCaptor.capture()
        );
        assertThat(cursorConditionCaptor.getValue().cursor()).isEqualTo(COACHING_MESSAGE_ID);
        assertThat(cursorConditionCaptor.getValue().size()).isEqualTo(CURSOR_PAGE_SIZE);
    }

    @Test
    void 코칭_메시지_목록_조회_시_코칭_대화가_없으면_404를_응답한다() {
        String url = COACHING_CONVERSATIONS_URL + "/" + UNKNOWN_COACHING_CONVERSATION_ID + "/messages";
        given(coachingConversationFinder.findMessagePage(
                eq(USER_ID),
                eq(UNKNOWN_COACHING_CONVERSATION_ID),
                any()
        )).willThrow(new ResourceNotFoundException(COACHING_CONVERSATION_NOT_FOUND));

        MvcTestResultAssert result = get(url, USER_ID).hasStatus(NOT_FOUND);

        assertErrorResponse(result, COACHING_CONVERSATION_NOT_FOUND);
        then(coachingConversationFinder).should().findMessagePage(
                eq(USER_ID),
                eq(UNKNOWN_COACHING_CONVERSATION_ID),
                any()
        );
    }

    @ParameterizedTest
    @CsvSource({
            "0, Size must be positive.",
            "101, Size must not exceed 100."
    })
    void 코칭_메시지_목록_조회_시_커서_페이징_요청_파라미터가_올바르지_않으면_400을_응답한다(int size, String message) {
        MvcTestResultAssert result = get(
                COACHING_MESSAGES_URL + "?size={size}",
                USER_ID,
                size
        ).hasStatus(BAD_REQUEST);

        assertInvalidCursorConditionResponse(result, message);

        then(coachingConversationFinder).shouldHaveNoInteractions();
    }

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

    @Test
    void 코칭_대화를_삭제한다() {
        delete(
                COACHING_CONVERSATION_URL,
                USER_ID,
                COACHING_CONVERSATION_ID
        ).hasStatus(NO_CONTENT);

        then(coachingConversationCreator).should().delete(COACHING_CONVERSATION_ID, USER_ID);
    }

    @Test
    void 코칭_대화_삭제_시_코칭_대화가_없으면_404를_응답한다() {
        willThrow(new ResourceNotFoundException(COACHING_CONVERSATION_NOT_FOUND))
                .given(coachingConversationCreator)
                .delete(UNKNOWN_COACHING_CONVERSATION_ID, USER_ID);

        MvcTestResultAssert result = delete(
                COACHING_CONVERSATION_URL,
                USER_ID,
                UNKNOWN_COACHING_CONVERSATION_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, COACHING_CONVERSATION_NOT_FOUND);
        then(coachingConversationCreator).should().delete(UNKNOWN_COACHING_CONVERSATION_ID, USER_ID);
    }

    @Test
    void AI_코치_응답_메시지_생성_중에_코칭_대화를_삭제하면_409를_응답한다() {
        willThrow(new ResourceConflictException(COACHING_RESPONSE_ALREADY_IN_PROGRESS))
                .given(coachingConversationCreator)
                .delete(COACHING_CONVERSATION_ID, USER_ID);

        MvcTestResultAssert result = delete(
                COACHING_CONVERSATION_URL,
                USER_ID,
                COACHING_CONVERSATION_ID
        ).hasStatus(CONFLICT);

        assertErrorResponse(result, COACHING_RESPONSE_ALREADY_IN_PROGRESS);
        then(coachingConversationCreator).should().delete(COACHING_CONVERSATION_ID, USER_ID);
    }

    @Test
    void AI_코치_응답과_대화_제목을_스트리밍한다() throws Exception {
        CoachingMessageSendRequest request = new CoachingMessageSendRequest(MESSAGE_CONTENT);
        CoachingMessageSendCommand command = request.to(USER_ID, COACHING_CONVERSATION_ID);
        CoachingResponseStream responseStream = mock(CoachingResponseStream.class);
        given(coachingMessageSender.send(command)).willReturn(new CoachingMessageSendResult(
                responseStream,
                completedFuture(Optional.of(CONVERSATION_TITLE))
        ));

        MvcResult mvcResult = sendMessage(request);
        CoachingResponseStream.Listener listener = captureListener(responseStream);

        listener.onDelta("좋");
        listener.onDelta("아요!");
        listener.onComplete();

        String response = getResponseBody(completeAsyncRequest(mvcResult));

        assertThat(response).containsSubsequence(
                "event:delta", "data:좋",
                "event:delta", "data:아요!",
                "event:conversationTitle", "data:" + CONVERSATION_TITLE,
                "event:done"
        );
        then(coachingMessageSender).should().send(command);
    }

    @Test
    void AI_코치_응답이_잘리면_미완료_사유를_스트리밍한다() throws Exception {
        CoachingMessageSendRequest request = new CoachingMessageSendRequest(MESSAGE_CONTENT);
        CoachingMessageSendCommand command = request.to(USER_ID, COACHING_CONVERSATION_ID);
        CoachingResponseStream responseStream = mock(CoachingResponseStream.class);
        given(coachingMessageSender.send(command)).willReturn(new CoachingMessageSendResult(
                responseStream,
                completedFuture(Optional.empty())
        ));

        MvcResult mvcResult = sendMessage(request);
        CoachingResponseStream.Listener listener = captureListener(responseStream);

        listener.onDelta("부분 응답");
        listener.onIncomplete(MAX_OUTPUT_TOKENS);

        String response = getResponseBody(completeAsyncRequest(mvcResult));

        assertThat(response)
                .containsSubsequence(
                        "event:delta", "data:부분 응답",
                        "event:incomplete", "data:max_output_tokens"
                )
                .doesNotContain("event:conversationTitle", "event:done");
        then(coachingMessageSender).should().send(command);
    }

    @Test
    void 회원_메시지_내용이_공백이면_400을_응답한다() {
        CoachingMessageSendRequest request = new CoachingMessageSendRequest(" ");

        postJson(COACHING_MESSAGES_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(coachingMessageSender).shouldHaveNoInteractions();
    }

    @Test
    void 회원_메시지_내용이_최대_길이를_초과하면_400을_응답한다() {
        CoachingMessageSendRequest request = new CoachingMessageSendRequest(
                "a".repeat(USER_MESSAGE_CONTENT_MAX_LENGTH + 1)
        );

        postJson(COACHING_MESSAGES_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(coachingMessageSender).shouldHaveNoInteractions();
    }

    @Test
    void AI_코치_응답과_대화_제목_스트리밍_시_코칭_대화가_없으면_404를_응답한다() {
        String url = COACHING_CONVERSATIONS_URL + "/" + UNKNOWN_COACHING_CONVERSATION_ID + "/messages";
        CoachingMessageSendRequest request = new CoachingMessageSendRequest(MESSAGE_CONTENT);
        CoachingMessageSendCommand command = request.to(USER_ID, UNKNOWN_COACHING_CONVERSATION_ID);
        willThrow(new ResourceNotFoundException(COACHING_CONVERSATION_NOT_FOUND))
                .given(coachingMessageSender)
                .send(command);

        MvcTestResultAssert result = mvcTester.post()
                .uri(url)
                .accept(TEXT_EVENT_STREAM)
                .contentType(APPLICATION_JSON)
                .content(toJson(request))
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(USER_ID))))
                .assertThat()
                .hasStatus(NOT_FOUND)
                .hasContentType(APPLICATION_JSON);

        assertErrorResponse(result, COACHING_CONVERSATION_NOT_FOUND);
        then(coachingMessageSender).should().send(command);
    }

    @Test
    void AI_코치_응답과_대화_제목_스트리밍_중_회원이_메시지_전송하면_409를_응답한다() {
        CoachingMessageSendRequest request = new CoachingMessageSendRequest(MESSAGE_CONTENT);
        CoachingMessageSendCommand command = request.to(USER_ID, COACHING_CONVERSATION_ID);
        willThrow(new ResourceConflictException(COACHING_RESPONSE_ALREADY_IN_PROGRESS))
                .given(coachingMessageSender)
                .send(command);

        MvcTestResultAssert result = mvcTester.post()
                .uri(COACHING_MESSAGES_URL)
                .accept(TEXT_EVENT_STREAM)
                .contentType(APPLICATION_JSON)
                .content(toJson(request))
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(USER_ID))))
                .assertThat()
                .hasStatus(CONFLICT)
                .hasContentType(APPLICATION_JSON);

        assertErrorResponse(result, COACHING_RESPONSE_ALREADY_IN_PROGRESS);
        then(coachingMessageSender).should().send(command);
    }

    private static void assertCoachingConversationsFindResponse(
            MvcTestResultAssert result,
            CursorResult<CoachingConversationSummaryResult, Long> summaryPage,
            CoachingConversationSummaryResult summary
    ) {
        result.bodyJson().extractingPath("$.cursorInfo.nextCursor")
                .isEqualTo(summaryPage.cursorInfo().nextCursor().intValue());
        result.bodyJson().extractingPath("$.cursorInfo.hasNext")
                .isEqualTo(summaryPage.cursorInfo().hasNext());
        result.bodyJson().extractingPath("$.coachingConversations[0].coachingConversationId")
                .isEqualTo(summary.coachingConversationId().intValue());
        result.bodyJson().extractingPath("$.coachingConversations[0].title")
                .isEqualTo(summary.title());
        result.bodyJson().extractingPath("$.coachingConversations[0].context")
                .isEqualTo(summary.context().name());
        result.bodyJson().extractingPath("$.coachingConversations[0].preview")
                .isEqualTo(summary.preview());
        result.bodyJson().extractingPath("$.coachingConversations[0].lastMessageCreatedAt")
                .isEqualTo(summary.lastMessageCreatedAt().toString());
        result.bodyJson().extractingPath("$.coachingConversations[0].createdAt")
                .isEqualTo(summary.createdAt().toString());
        result.bodyJson().extractingPath("$.coachingConversations[0].updatedAt")
                .isEqualTo(summary.updatedAt().toString());
    }

    private static void assertCoachingMessagesFindResponse(
            MvcTestResultAssert result,
            CursorResult<CoachingMessageResult, Long> messagePage,
            CoachingMessageResult message
    ) {
        result.bodyJson().extractingPath("$.cursorInfo.nextCursor")
                .isEqualTo(messagePage.cursorInfo().nextCursor().intValue());
        result.bodyJson().extractingPath("$.cursorInfo.hasNext")
                .isEqualTo(messagePage.cursorInfo().hasNext());
        result.bodyJson().extractingPath("$.coachingMessages[0].coachingMessageId")
                .isEqualTo(message.coachingMessageId().intValue());
        result.bodyJson().extractingPath("$.coachingMessages[0].role")
                .isEqualTo(message.role().name());
        result.bodyJson().extractingPath("$.coachingMessages[0].content")
                .isEqualTo(message.content());
        result.bodyJson().extractingPath("$.coachingMessages[0].completionStatus")
                .isEqualTo(message.completionStatus().name());
        result.bodyJson().extractingPath("$.coachingMessages[0].createdAt")
                .isEqualTo(message.createdAt().toString());
    }

    private void assertInvalidCursorConditionResponse(MvcTestResultAssert result, String message) {
        assertErrorResponse(result, INVALID_INPUT_VALUE);
        result.bodyJson()
                .extractingPath("$.errors[0].field")
                .isEqualTo("argument");
        result.bodyJson()
                .extractingPath("$.errors[0].message")
                .isEqualTo(message);
    }

    private MvcResult sendMessage(CoachingMessageSendRequest request) throws Exception {
        return mockMvc.perform(post(COACHING_MESSAGES_URL)
                        .accept(TEXT_EVENT_STREAM)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request))
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(USER_ID)))))
                .andExpect(request().asyncStarted())
                .andReturn();
    }

    private CoachingResponseStream.Listener captureListener(CoachingResponseStream responseStream) {
        ArgumentCaptor<CoachingResponseStream.Listener> listenerCaptor =
                ArgumentCaptor.forClass(CoachingResponseStream.Listener.class);
        then(responseStream).should().subscribe(listenerCaptor.capture());

        return listenerCaptor.getValue();
    }

    private MvcResult completeAsyncRequest(MvcResult mvcResult) throws Exception {
        return mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(TEXT_EVENT_STREAM))
                .andReturn();
    }

    private String getResponseBody(MvcResult mvcResult) {
        return new String(mvcResult.getResponse().getContentAsByteArray(), UTF_8);
    }
}
