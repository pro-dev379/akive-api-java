package net.akive.api.coaching.adapter.llm;

import com.openai.client.OpenAIClient;
import lombok.RequiredArgsConstructor;
import net.akive.api.coaching.application.required.CoachingLlm;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class OpenAiCoachingLlmAdapter implements CoachingLlm {

    private final OpenAIClient openAIClient;

    @Override
    public String createConversation() {
        return openAIClient.conversations().create().id();
    }
}
