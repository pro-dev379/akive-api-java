package net.akive.api.coaching.adapter.llm;

import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.conversations.Conversation;
import com.openai.services.blocking.ConversationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.LLM_CONVERSATION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class OpenAiCoachingLlmAdapterTest {

    @Mock
    OpenAIClient openAIClient;

    @Mock
    ConversationService conversationService;

    @InjectMocks
    OpenAiCoachingLlmAdapter openAiCoachingLlmAdapter;

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

        then(openAIClient).should().conversations();
        then(conversationService).should().create();
    }
}
