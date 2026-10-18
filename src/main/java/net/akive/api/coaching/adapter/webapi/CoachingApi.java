package net.akive.api.coaching.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.coaching.adapter.webapi.request.CoachingConversationCreateRequest;
import net.akive.api.coaching.adapter.webapi.request.CoachingMessageSendRequest;
import net.akive.api.coaching.adapter.webapi.response.CoachingConversationCreateResponse;
import net.akive.api.coaching.adapter.webapi.response.CoachingConversationsFindResponse;
import net.akive.api.coaching.application.provided.CoachingConversationCreator;
import net.akive.api.coaching.application.provided.CoachingConversationFinder;
import net.akive.api.coaching.application.provided.CoachingMessageSender;
import net.akive.api.coaching.application.provided.dto.CoachingConversationSummaryResult;
import net.akive.api.coaching.application.provided.dto.CoachingMessageSendResult;
import net.akive.api.coaching.application.stream.CoachingResponseStream;
import net.akive.api.coaching.domain.ConversationContext;
import net.akive.api.common.pagination.CursorResult;
import net.akive.api.core.config.security.AuthenticatedUserId;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;
import static net.akive.api.common.pagination.CursorCondition.of;

@RestController
@RequestMapping("/coaching")
@RequiredArgsConstructor
public class CoachingApi {

    private final CoachingConversationCreator coachingConversationCreator;
    private final CoachingConversationFinder coachingConversationFinder;
    private final CoachingMessageSender coachingMessageSender;

    @GetMapping("/conversations")
    public CoachingConversationsFindResponse findRecentConversations(
            @AuthenticatedUserId Long userId,
            @RequestParam ConversationContext context,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer size
    ) {
        CursorResult<CoachingConversationSummaryResult, Long> summaryPage =
                coachingConversationFinder.findSummaryPage(userId, context, of(cursor, size));

        return CoachingConversationsFindResponse.from(summaryPage);
    }

    @PostMapping("/conversations")
    @ResponseStatus(CREATED)
    public CoachingConversationCreateResponse create(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody CoachingConversationCreateRequest request
    ) {
        Long coachingConversationId = coachingConversationCreator.create(request.to(userId));

        return new CoachingConversationCreateResponse(coachingConversationId);
    }

    @PostMapping(
            value = "/conversations/{coachingConversationId}/messages",
            produces = TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter sendMessage(
            @AuthenticatedUserId Long userId,
            @PathVariable Long coachingConversationId,
            @Validated @RequestBody CoachingMessageSendRequest request
    ) {
        CoachingMessageSendResult result = coachingMessageSender.send(request.to(userId, coachingConversationId));

        return toSseEmitter(result);
    }

    private SseEmitter toSseEmitter(CoachingMessageSendResult result) {
        CoachingResponseStream coachingResponseStream = result.responseStream();
        SseEmitter emitter = new SseEmitter(0L);

        emitter.onTimeout(coachingResponseStream::cancel);
        emitter.onError(error -> coachingResponseStream.cancel());
        emitter.onCompletion(coachingResponseStream::cancel);

        SseListener sseListener = new SseListener(emitter, coachingResponseStream, result.conversationTitle());
        coachingResponseStream.subscribe(sseListener);

        return emitter;
    }

    private record SseListener(
            SseEmitter emitter,
            CoachingResponseStream coachingResponseStream,
            CompletionStage<Optional<String>> conversationTitle
    ) implements CoachingResponseStream.Listener {

        @Override
        public void onDelta(String delta) {
            try {
                emitter.send(SseEmitter.event().name("delta").data(delta));
            } catch (IOException e) {
                coachingResponseStream.cancel();
                emitter.completeWithError(e);
            }
        }

        @Override
        public void onComplete() {
            try {
                sendTitleIfPresent();
                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }

        @Override
        public void onIncomplete(CoachingResponseStream.IncompleteReason reason) {
            try {
                sendTitleIfPresent();
                emitter.send(SseEmitter.event().name("incomplete").data(reason.code()));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }

        @Override
        public void onError(Throwable error) {
            emitter.completeWithError(error);
        }

        private void sendTitleIfPresent() throws IOException {
            Optional<String> conversationTitle = conversationTitle().toCompletableFuture().join();

            if (conversationTitle.isPresent()) {
                emitter.send(SseEmitter.event().name("conversationTitle").data(conversationTitle.get()));
            }
        }
    }
}
