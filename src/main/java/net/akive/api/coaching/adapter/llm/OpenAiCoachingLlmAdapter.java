package net.akive.api.coaching.adapter.llm;

import com.openai.client.OpenAIClient;
import com.openai.core.RequestOptions;
import com.openai.core.http.AsyncStreamResponse;
import com.openai.models.conversations.ConversationDeletedResource;
import com.openai.models.responses.*;
import com.openai.errors.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.akive.api.coaching.application.prompt.CoachingPrompt;
import net.akive.api.coaching.application.required.CoachingLlm;
import net.akive.api.coaching.application.stream.CoachingResponseStream;
import net.akive.api.core.config.llm.OpenAiProperties;
import net.akive.api.llmusage.application.provided.LlmUsageRecorder;
import net.akive.api.llmusage.application.provided.dto.LlmUsageRecordCommand;
import net.akive.api.llmusage.domain.LlmUsagePurpose;
import net.akive.api.llmusage.domain.LlmUsageResultStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.stream.Collectors.joining;
import static net.akive.api.coaching.application.stream.CoachingResponseStream.IncompleteReason.MAX_OUTPUT_TOKENS;
import static net.akive.api.coaching.domain.CoachingPolicy.COACHING_OUTPUT_TOKEN_MAX_COUNT;
import static net.akive.api.coaching.domain.CoachingPolicy.TITLE_OUTPUT_TOKEN_MAX_COUNT;
import static net.akive.api.llmusage.domain.LlmProvider.OPENAI;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.COACHING_RESPONSE;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.COACHING_TITLE;
import static net.akive.api.llmusage.domain.LlmUsageResultStatus.COMPLETED;
import static net.akive.api.llmusage.domain.LlmUsageResultStatus.FAILED;
import static net.akive.api.llmusage.domain.LlmUsageResultStatus.INCOMPLETE;
import static net.akive.api.llmusage.domain.LlmUsageScope.USER;

@Component
@Slf4j
@RequiredArgsConstructor
class OpenAiCoachingLlmAdapter implements CoachingLlm {

    private final OpenAIClient openAIClient;
    private final OpenAiProperties openAiProperties;
    private final LlmUsageRecorder llmUsageRecorder;

    @Override
    public String createConversation() {
        return openAIClient.conversations().create().id();
    }

    @Override
    public CompletionStage<String> generateConversationTitle(Long userId, CoachingPrompt coachingPrompt) {
        return openAIClient.async()
                .responses()
                .create(
                        createConversationTitleParams(coachingPrompt),
                        requestOptions(openAiProperties.timeout())
                )
                .thenApply(response -> {
                    new OpenAiUsageRecording(
                            llmUsageRecorder,
                            userId,
                            COACHING_TITLE,
                            openAiProperties.model().conversationTitle()
                    ).recordSafely(response, resultStatus(response));

                    return extractConversationTitle(response);
                });
    }

    @Override
    public CoachingResponseStream streamResponse(
            Long userId,
            String conversationId,
            CoachingPrompt coachingPrompt
    ) {
        AsyncStreamResponse<ResponseStreamEvent> openAiStream = openAIClient.async()
                .responses()
                .createStreaming(
                        createCoachingParams(conversationId, coachingPrompt),
                        requestOptions(openAiProperties.streamingTimeout())
                );

        return new OpenAiResponseStream(
                openAiStream,
                new OpenAiUsageRecording(
                        llmUsageRecorder,
                        userId,
                        COACHING_RESPONSE,
                        openAiProperties.model().coaching()
                )
        );
    }

    @Override
    public void deleteConversation(String conversationId) {
        try {
            ConversationDeletedResource deletedConversation = openAIClient.conversations().delete(
                    conversationId,
                    requestOptions(openAiProperties.timeout())
            );

            if (!deletedConversation.deleted()) {
                throw new IllegalStateException("OpenAI did not delete the coaching conversation: " + conversationId);
            }
        } catch (NotFoundException ignored) {
            log.info("OpenAI coaching conversation is already deleted: conversationId={}", conversationId);
        }
    }

    private ResponseCreateParams createCoachingParams(String conversationId, CoachingPrompt coachingPrompt) {
        return ResponseCreateParams.builder()
                .model(openAiProperties.model().coaching())
                .conversation(conversationId)
                .instructions(coachingPrompt.instructions())
                .input(coachingPrompt.input())
                .maxOutputTokens(COACHING_OUTPUT_TOKEN_MAX_COUNT)
                .build();
    }

    private ResponseCreateParams createConversationTitleParams(CoachingPrompt coachingPrompt) {
        return ResponseCreateParams.builder()
                .model(openAiProperties.model().conversationTitle())
                .instructions(coachingPrompt.instructions())
                .input(coachingPrompt.input())
                .maxOutputTokens(TITLE_OUTPUT_TOKEN_MAX_COUNT)
                .store(false)
                .build();
    }

    private static RequestOptions requestOptions(Duration timeout) {
        return RequestOptions.builder()
                .timeout(timeout)
                .build();
    }

    private String extractConversationTitle(Response response) {
        if (response.status().filter(ResponseStatus.COMPLETED::equals).isEmpty()) {
            throw conversationTitleGenerationException(response);
        }

        String conversationTitle = extractText(response);

        if (conversationTitle.isBlank()) {
            throw new IllegalStateException("OpenAI returned an empty coaching conversation conversationTitle.");
        }

        return conversationTitle;
    }

    private String extractText(Response response) {
        return response.output().stream()
                .flatMap(output -> output.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(responseContent -> responseContent.outputText().stream())
                .map(ResponseOutputText::text)
                .collect(joining())
                .trim();
    }

    private static IllegalStateException conversationTitleGenerationException(Response response) {
        String status = response.status()
                .map(ResponseStatus::asString)
                .orElse("missing");
        String incompleteReason = response.incompleteDetails()
                .flatMap(Response.IncompleteDetails::reason)
                .map(Response.IncompleteDetails.Reason::asString)
                .orElse("none");
        String errorMessage = response.error()
                .map(ResponseError::message)
                .orElse("none");
        String usage = response.usage()
                .map(Object::toString)
                .orElse("none");

        return new IllegalStateException(
                "OpenAI failed to generate a coaching conversation conversationTitle: "
                        + "status=" + status
                        + ", incompleteReason=" + incompleteReason
                        + ", error=" + errorMessage
                        + ", usage=" + usage
        );
    }

    private static LlmUsageResultStatus resultStatus(Response response) {
        return response.status()
                .map(status -> {
                    if (status.equals(ResponseStatus.COMPLETED)) {
                        return COMPLETED;
                    }
                    if (status.equals(ResponseStatus.INCOMPLETE)) {
                        return INCOMPLETE;
                    }
                    return FAILED;
                })
                .orElse(FAILED);
    }

    private record OpenAiUsageRecording(
            LlmUsageRecorder llmUsageRecorder,
            Long userId,
            LlmUsagePurpose purpose,
            String modelName
    ) {

        private void recordSafely(Response response, LlmUsageResultStatus resultStatus) {
            response.usage().ifPresent(usage -> {
                try {
                    llmUsageRecorder.record(new LlmUsageRecordCommand(
                            userId,
                            purpose,
                            OPENAI,
                            response.id(),
                            modelName,
                            USER,
                            resultStatus,
                            usage.inputTokens(),
                            usage.inputTokensDetails().cachedTokens(),
                            usage.outputTokens(),
                            usage.outputTokensDetails().reasoningTokens()
                    ));
                } catch (RuntimeException e) {
                    log.warn(
                            "Failed to record OpenAI usage: userId={}, purpose={}, providerResponseId={}",
                            userId,
                            purpose,
                            response.id(),
                            e
                    );
                }
            });
        }
    }

    private record OpenAiResponseStream(
            AsyncStreamResponse<ResponseStreamEvent> openAiStream,
            OpenAiUsageRecording usageRecording
    ) implements CoachingResponseStream {

        @Override
        public void subscribe(Listener downstreamListener) {
            openAiStream.subscribe(new OpenAiStreamHandler(downstreamListener, usageRecording));
        }

        @Override
        public void cancel() {
            openAiStream.close();
        }
    }

    private static final class OpenAiStreamHandler implements AsyncStreamResponse.Handler<ResponseStreamEvent> {

        private final CoachingResponseStream.Listener downstreamListener;
        private final OpenAiUsageRecording usageRecording;
        private final AtomicBoolean terminated = new AtomicBoolean();

        private OpenAiStreamHandler(
                CoachingResponseStream.Listener downstreamListener,
                OpenAiUsageRecording usageRecording
        ) {
            this.downstreamListener = downstreamListener;
            this.usageRecording = usageRecording;
        }

        @Override
        public void onNext(ResponseStreamEvent event) {
            if (terminated.get()) {
                return;
            }

            event.outputTextDelta().ifPresent(delta -> downstreamListener.onDelta(delta.delta()));
            event.refusalDelta().ifPresent(delta -> downstreamListener.onDelta(delta.delta()));
            event.error().ifPresent(error -> fail(new IllegalStateException(error.message())));
            event.failed().ifPresent(failed -> fail(
                    failed.response(),
                    new IllegalStateException(
                            failed.response().error()
                                    .map(ResponseError::message)
                                    .orElse("OpenAI response failed.")
                    )
            ));
            event.incomplete().ifPresent(incomplete -> handleIncomplete(incomplete.response()));
            event.completed().ifPresent(completed -> complete(completed.response()));
        }

        @Override
        public void onComplete(Optional<Throwable> error) {
            fail(error.orElseGet(() -> new IllegalStateException(
                    "OpenAI response stream ended without a terminal event."
            )));
        }

        private void complete(Response response) {
            if (terminated.compareAndSet(false, true)) {
                usageRecording.recordSafely(response, COMPLETED);
                downstreamListener.onComplete();
            }
        }

        private void handleIncomplete(Response response) {
            if (!terminated.compareAndSet(false, true)) {
                return;
            }

            usageRecording.recordSafely(response, INCOMPLETE);

            boolean reachedMaxOutputTokens = response.incompleteDetails()
                    .flatMap(Response.IncompleteDetails::reason)
                    .filter(Response.IncompleteDetails.Reason.MAX_OUTPUT_TOKENS::equals)
                    .isPresent();

            if (reachedMaxOutputTokens) {
                downstreamListener.onIncomplete(MAX_OUTPUT_TOKENS);

                return;
            }

            String reason = response.incompleteDetails()
                    .flatMap(Response.IncompleteDetails::reason)
                    .map(Response.IncompleteDetails.Reason::asString)
                    .orElse("unknown");

            downstreamListener.onError(new IllegalStateException(
                    "OpenAI response was incomplete: reason=" + reason
            ));
        }

        private void fail(Throwable error) {
            if (terminated.compareAndSet(false, true)) {
                downstreamListener.onError(error);
            }
        }

        private void fail(Response response, Throwable error) {
            if (terminated.compareAndSet(false, true)) {
                usageRecording.recordSafely(response, FAILED);
                downstreamListener.onError(error);
            }
        }
    }
}
