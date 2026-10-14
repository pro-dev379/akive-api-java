package net.akive.api.coaching.adapter.webapi;

import net.akive.api.coaching.adapter.webapi.request.CoachingConversationCreateRequest;
import net.akive.api.coaching.adapter.webapi.request.CoachingMessageSendRequest;
import net.akive.api.coaching.application.provided.CoachingConversationCreator;
import net.akive.api.coaching.application.provided.CoachingMessageSender;
import net.akive.api.coaching.application.provided.dto.CoachingConversationCreateCommand;
import net.akive.api.coaching.application.provided.dto.CoachingMessageSendCommand;
import net.akive.api.coaching.application.provided.dto.CoachingMessageSendResult;
import net.akive.api.coaching.application.stream.CoachingResponseStream;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.Optional;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.akive.api.coaching.application.stream.CoachingResponseStream.IncompleteReason.MAX_OUTPUT_TOKENS;
import static net.akive.api.coaching.domain.ConversationContext.MAIN;
import static net.akive.api.coaching.domain.CoachingPolicy.USER_MESSAGE_CONTENT_MAX_LENGTH;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.ANOTHER_COACHING_CONVERSATION_ID;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.COACHING_CONVERSATION_ID;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.CONVERSATION_TITLE;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.MESSAGE_CONTENT;
import static net.akive.api.common.exception.business.ResourceNotFoundError.COACHING_CONVERSATION_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceConflictError.COACHING_RESPONSE_ALREADY_IN_PROGRESS;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CoachingApi.class)
class CoachingApiTest extends PrivateApiTestSupport {

    private static final String COACHING_CONVERSATIONS_URL = "/coaching/conversations";
    private static final String COACHING_MESSAGES_URL =
            COACHING_CONVERSATIONS_URL + "/" + COACHING_CONVERSATION_ID + "/messages";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CoachingConversationCreator coachingConversationCreator;

    @MockitoBean
    CoachingMessageSender coachingMessageSender;

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
    void 코칭_대화를_조회할_수_없으면_메시지_전송_시_404를_응답한다() {
        String url = COACHING_CONVERSATIONS_URL + "/" + ANOTHER_COACHING_CONVERSATION_ID + "/messages";
        CoachingMessageSendRequest request = new CoachingMessageSendRequest(MESSAGE_CONTENT);
        CoachingMessageSendCommand command = request.to(USER_ID, ANOTHER_COACHING_CONVERSATION_ID);
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
