package net.akive.api.coaching.application.provided.dto;

import net.akive.api.coaching.application.stream.CoachingResponseStream;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public record CoachingMessageSendResult(
        CoachingResponseStream responseStream,
        CompletionStage<Optional<String>> conversationTitle
) {
}
