package net.akive.api.coaching.application.required;

import net.akive.api.coaching.application.prompt.CoachingPrompt;
import net.akive.api.coaching.application.stream.CoachingResponseStream;

import java.util.concurrent.CompletionStage;

public interface CoachingLlm {

    String createConversation();

    CompletionStage<String> generateConversationTitle(Long userId, CoachingPrompt coachingPrompt);

    CoachingResponseStream streamResponse(Long userId, String conversationId, CoachingPrompt coachingPrompt);
}
