package net.akive.api.coaching.adapter.llm;

import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientAsync;
import com.openai.core.JsonValue;
import com.openai.core.RequestOptions;
import com.openai.core.http.AsyncStreamResponse;
import com.openai.models.conversations.Conversation;
import com.openai.models.responses.*;
import com.openai.services.async.ResponseServiceAsync;
import com.openai.services.blocking.ConversationService;
import net.akive.api.coaching.application.prompt.CoachingPrompt;
import net.akive.api.coaching.application.stream.CoachingResponseStream;
import net.akive.api.llmusage.application.provided.LlmUsageRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static net.akive.api.core.config.llm.fixture.OpenAiPropertiesFixture.COACHING_MODEL;
import static net.akive.api.core.config.llm.fixture.OpenAiPropertiesFixture.CONVERSATION_TITLE_MODEL;
import static net.akive.api.core.config.llm.fixture.OpenAiPropertiesFixture.createOpenAiProperties;
import static net.akive.api.core.config.llm.fixture.OpenAiResponseFixture.*;
import static net.akive.api.coaching.application.stream.CoachingResponseStream.IncompleteReason.MAX_OUTPUT_TOKENS;
import static net.akive.api.coaching.domain.CoachingPolicy.COACHING_OUTPUT_TOKEN_MAX_COUNT;
import static net.akive.api.coaching.domain.CoachingPolicy.TITLE_OUTPUT_TOKEN_MAX_COUNT;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.CONVERSATION_TITLE;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.LLM_CONVERSATION_ID;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.COACHING_RESPONSE;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.COACHING_TITLE;
import static net.akive.api.llmusage.domain.LlmUsageResultStatus.COMPLETED;
import static net.akive.api.llmusage.domain.LlmUsageResultStatus.INCOMPLETE;
import static net.akive.api.llmusage.domain.LlmUsageScope.USER;
import static net.akive.api.llmusage.application.fixture.LlmUsageRecordCommandFixture.createLlmUsageRecordCommand;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OpenAiCoachingLlmAdapterTest {

    private static final String RESPONSE_ID = "resp_123";
    private static final String INSTRUCTIONS = "학습자의 실행을 돕는 코치로 답변하세요.";
    private static final String INPUT = "학습 계획을 세우고 싶어요.";
    private static final String RESPONSE_DELTA = "계획을 작은 단위로 나누어 보세요.";

    @Mock
    OpenAIClient openAIClient;

    @Mock
    OpenAIClientAsync openAIClientAsync;

    @Mock
    ConversationService conversationService;

    @Mock
    ResponseServiceAsync responseService;

    @Mock
    AsyncStreamResponse<ResponseStreamEvent> openAiStream;

    @Mock
    LlmUsageRecorder llmUsageRecorder;

    @Mock
    CoachingResponseStream.Listener responseListener;

    @Captor
    ArgumentCaptor<AsyncStreamResponse.Handler<ResponseStreamEvent>> streamHandlerCaptor;

    OpenAiCoachingLlmAdapter openAiCoachingLlmAdapter;

    @BeforeEach
    void setUp() {
        openAiCoachingLlmAdapter = new OpenAiCoachingLlmAdapter(
                openAIClient,
                createOpenAiProperties(),
                llmUsageRecorder
        );
    }

    @Test
    void OpenAI_기반_코칭_대화를_생성한다() {
        Conversation conversation = Conversation.builder()
                .id(LLM_CONVERSATION_ID)
                .createdAt(1L)
                .metadata(JsonValue.from(Map.of()))
                .object_(JsonValue.from("conversation"))
                .build();
        given(openAIClient.conversations()).willReturn(conversationService);
        given(conversationService.create()).willReturn(conversation);

        String llmConversationId = openAiCoachingLlmAdapter.createConversation();

        assertThat(llmConversationId).isEqualTo(LLM_CONVERSATION_ID);
        then(conversationService).should().create();
    }

    @Test
    void 회원_메시지를_기반으로_코칭_대화_제목을_생성하고_LLM_토큰_사용량을_기록한다() {
        CoachingPrompt coachingPrompt = createCoachingPrompt();
        Response response = createTitleResponse(CONVERSATION_TITLE);
        given(response.usage()).willReturn(Optional.of(createResponseUsage()));
        prepareResponseService();
        given(responseService.create(any(ResponseCreateParams.class), any(RequestOptions.class)))
                .willReturn(completedFuture(response));

        String generatedTitle = openAiCoachingLlmAdapter
                .generateConversationTitle(USER_ID, coachingPrompt)
                .toCompletableFuture()
                .join();

        assertThat(generatedTitle).isEqualTo(CONVERSATION_TITLE);
        assertConversationTitleParams(captureCreatedParams(), coachingPrompt);
        then(llmUsageRecorder).should().record(createLlmUsageRecordCommand(
                USER_ID,
                COACHING_TITLE,
                RESPONSE_ID,
                CONVERSATION_TITLE_MODEL,
                USER,
                COMPLETED
        ));
    }

    @Test
    void 완료되지_않은_OpenAI_응답으로는_코칭_대화_제목을_생성할_수_없다() {
        Response response = mock(Response.class);
        given(response.status()).willReturn(Optional.of(ResponseStatus.INCOMPLETE));
        prepareResponseService();
        given(responseService.create(any(ResponseCreateParams.class), any(RequestOptions.class)))
                .willReturn(completedFuture(response));

        assertThatThrownBy(() -> openAiCoachingLlmAdapter
                .generateConversationTitle(USER_ID, createCoachingPrompt())
                .toCompletableFuture()
                .join())
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage(
                        "OpenAI failed to generate a coaching conversation conversationTitle: "
                                + "status=incomplete, incompleteReason=none, error=none, usage=none"
                );
    }

    @Test
    void OpenAI_코칭_응답을_스트리밍하고_LLM_토큰_사용량을_기록한다() {
        CoachingPrompt coachingPrompt = createCoachingPrompt();
        CoachingResponseStream responseStream = createResponseStream(coachingPrompt);
        responseStream.subscribe(responseListener);
        AsyncStreamResponse.Handler<ResponseStreamEvent> streamHandler = captureStreamHandler();

        ResponseTextDeltaEvent delta = mock(ResponseTextDeltaEvent.class);
        given(delta.delta()).willReturn(RESPONSE_DELTA);
        ResponseStreamEvent deltaEvent = mock(ResponseStreamEvent.class);
        given(deltaEvent.outputTextDelta()).willReturn(Optional.of(delta));

        Response response = createTerminalResponse();
        ResponseCompletedEvent completedEvent = mock(ResponseCompletedEvent.class);
        given(completedEvent.response()).willReturn(response);
        ResponseStreamEvent terminalEvent = mock(ResponseStreamEvent.class);
        given(terminalEvent.completed()).willReturn(Optional.of(completedEvent));

        streamHandler.onNext(deltaEvent);
        streamHandler.onNext(terminalEvent);
        streamHandler.onComplete(Optional.empty());

        then(responseListener).should().onDelta(RESPONSE_DELTA);
        then(responseListener).should().onComplete();
        then(responseListener).should(never()).onError(any());
        assertCoachingParams(captureStreamingParams(), coachingPrompt);
        then(llmUsageRecorder).should().record(createLlmUsageRecordCommand(
                USER_ID,
                COACHING_RESPONSE,
                RESPONSE_ID,
                COACHING_MODEL,
                USER,
                COMPLETED
        ));
    }

    @Test
    void 최대_출력_토큰에_도달하면_코칭_응답의_미완료_사유를_전달한다() {
        CoachingResponseStream responseStream = createResponseStream(createCoachingPrompt());
        responseStream.subscribe(responseListener);
        AsyncStreamResponse.Handler<ResponseStreamEvent> streamHandler = captureStreamHandler();

        Response.IncompleteDetails incompleteDetails = mock(Response.IncompleteDetails.class);
        given(incompleteDetails.reason()).willReturn(Optional.of(
                Response.IncompleteDetails.Reason.MAX_OUTPUT_TOKENS
        ));
        Response response = createTerminalResponse();
        given(response.incompleteDetails()).willReturn(Optional.of(incompleteDetails));
        ResponseIncompleteEvent incompleteEvent = mock(ResponseIncompleteEvent.class);
        given(incompleteEvent.response()).willReturn(response);
        ResponseStreamEvent streamEvent = mock(ResponseStreamEvent.class);
        given(streamEvent.incomplete()).willReturn(Optional.of(incompleteEvent));

        streamHandler.onNext(streamEvent);

        then(responseListener).should().onIncomplete(MAX_OUTPUT_TOKENS);
        then(responseListener).should(never()).onError(any());
        then(llmUsageRecorder).should().record(createLlmUsageRecordCommand(
                USER_ID,
                COACHING_RESPONSE,
                RESPONSE_ID,
                COACHING_MODEL,
                USER,
                INCOMPLETE
        ));
    }

    @Test
    void 종료_이벤트_없이_OpenAI_스트림이_끝나면_오류를_전달한다() {
        CoachingResponseStream responseStream = createResponseStream(createCoachingPrompt());
        responseStream.subscribe(responseListener);
        AsyncStreamResponse.Handler<ResponseStreamEvent> streamHandler = captureStreamHandler();

        streamHandler.onComplete(Optional.empty());

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        then(responseListener).should().onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("OpenAI response stream ended without a terminal event.");
    }

    @Test
    void 코칭_응답_스트림을_취소하면_OpenAI_스트림을_종료한다() {
        CoachingResponseStream responseStream = createResponseStream(createCoachingPrompt());

        responseStream.cancel();

        then(openAiStream).should().close();
    }

    private void prepareResponseService() {
        given(openAIClient.async()).willReturn(openAIClientAsync);
        given(openAIClientAsync.responses()).willReturn(responseService);
    }

    private CoachingResponseStream createResponseStream(CoachingPrompt coachingPrompt) {
        prepareResponseService();
        given(responseService.createStreaming(any(ResponseCreateParams.class), any(RequestOptions.class)))
                .willReturn(openAiStream);

        return openAiCoachingLlmAdapter.streamResponse(
                USER_ID,
                LLM_CONVERSATION_ID,
                coachingPrompt
        );
    }

    private Response createTitleResponse(String title) {
        ResponseOutputText outputText = mock(ResponseOutputText.class);
        given(outputText.text()).willReturn("  " + title + "  ");
        ResponseOutputMessage.Content content = mock(ResponseOutputMessage.Content.class);
        given(content.outputText()).willReturn(Optional.of(outputText));
        ResponseOutputMessage message = mock(ResponseOutputMessage.class);
        given(message.content()).willReturn(List.of(content));
        ResponseOutputItem outputItem = mock(ResponseOutputItem.class);
        given(outputItem.message()).willReturn(Optional.of(message));
        Response response = mock(Response.class);
        given(response.id()).willReturn(RESPONSE_ID);
        given(response.status()).willReturn(Optional.of(ResponseStatus.COMPLETED));
        given(response.output()).willReturn(List.of(outputItem));

        return response;
    }

    private Response createTerminalResponse() {
        Response response = mock(Response.class);
        given(response.id()).willReturn(RESPONSE_ID);
        given(response.usage()).willReturn(Optional.of(createResponseUsage()));

        return response;
    }

    private CoachingPrompt createCoachingPrompt() {
        return new CoachingPrompt(INSTRUCTIONS, INPUT);
    }

    private ResponseCreateParams captureCreatedParams() {
        ArgumentCaptor<ResponseCreateParams> paramsCaptor = ArgumentCaptor.forClass(ResponseCreateParams.class);
        then(responseService).should().create(paramsCaptor.capture(), any(RequestOptions.class));

        return paramsCaptor.getValue();
    }

    private ResponseCreateParams captureStreamingParams() {
        ArgumentCaptor<ResponseCreateParams> paramsCaptor = ArgumentCaptor.forClass(ResponseCreateParams.class);
        then(responseService).should().createStreaming(paramsCaptor.capture(), any(RequestOptions.class));

        return paramsCaptor.getValue();
    }

    private AsyncStreamResponse.Handler<ResponseStreamEvent> captureStreamHandler() {
        then(openAiStream).should().subscribe(streamHandlerCaptor.capture());

        return streamHandlerCaptor.getValue();
    }

    private void assertConversationTitleParams(ResponseCreateParams params, CoachingPrompt coachingPrompt) {
        assertThat(params.model().orElseThrow().asString()).isEqualTo(CONVERSATION_TITLE_MODEL);
        assertThat(params.instructions()).contains(coachingPrompt.instructions());
        assertThat(params.input().orElseThrow().asText()).isEqualTo(coachingPrompt.input());
        assertThat(params.maxOutputTokens()).contains(TITLE_OUTPUT_TOKEN_MAX_COUNT);
        assertThat(params.store()).contains(false);
        assertThat(params.conversation()).isEmpty();
    }

    private void assertCoachingParams(ResponseCreateParams params, CoachingPrompt coachingPrompt) {
        assertThat(params.model().orElseThrow().asString()).isEqualTo(COACHING_MODEL);
        assertThat(params.conversation().orElseThrow().asId()).isEqualTo(LLM_CONVERSATION_ID);
        assertThat(params.instructions()).contains(coachingPrompt.instructions());
        assertThat(params.input().orElseThrow().asText()).isEqualTo(coachingPrompt.input());
        assertThat(params.maxOutputTokens()).contains(COACHING_OUTPUT_TOKEN_MAX_COUNT);
    }

}
