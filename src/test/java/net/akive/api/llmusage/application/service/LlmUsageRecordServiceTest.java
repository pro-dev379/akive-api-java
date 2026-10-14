package net.akive.api.llmusage.application.service;

import net.akive.api.llmusage.application.provided.dto.LlmUsageRecordCommand;
import net.akive.api.llmusage.application.required.LlmUsageLogRepository;
import net.akive.api.llmusage.domain.LlmUsageLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.llmusage.application.fixture.LlmUsageRecordCommandFixture.createLlmUsageRecordCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LlmUsageRecordServiceTest {

    @Mock
    LlmUsageLogRepository llmUsageLogRepository;

    @InjectMocks
    LlmUsageRecordService llmUsageRecordService;

    @Test
    void LLM_토큰_사용량을_로그로_저장한다() {
        LlmUsageRecordCommand command = createLlmUsageRecordCommand();

        llmUsageRecordService.record(command);

        ArgumentCaptor<LlmUsageLog> llmUsageLogCaptor = ArgumentCaptor.forClass(LlmUsageLog.class);
        then(llmUsageLogRepository).should().save(llmUsageLogCaptor.capture());
        LlmUsageLog llmUsageLog = llmUsageLogCaptor.getValue();
        assertThat(llmUsageLog.getLlmUsageLogId()).isNull();
        assertThat(llmUsageLog.getUserId()).isEqualTo(command.userId());
        assertThat(llmUsageLog.getPurpose()).isEqualTo(command.purpose());
        assertThat(llmUsageLog.getProvider()).isEqualTo(command.provider());
        assertThat(llmUsageLog.getProviderResponseId()).isEqualTo(command.providerResponseId());
        assertThat(llmUsageLog.getModelName()).isEqualTo(command.modelName());
        assertThat(llmUsageLog.getUsageScope()).isEqualTo(command.usageScope());
        assertThat(llmUsageLog.getResultStatus()).isEqualTo(command.resultStatus());
        assertThat(llmUsageLog.getInputTokenCount()).isEqualTo(command.inputTokenCount());
        assertThat(llmUsageLog.getCachedInputTokenCount()).isEqualTo(command.cachedInputTokenCount());
        assertThat(llmUsageLog.getOutputTokenCount()).isEqualTo(command.outputTokenCount());
        assertThat(llmUsageLog.getReasoningOutputTokenCount()).isEqualTo(command.reasoningOutputTokenCount());
        assertThat(llmUsageLog.getCreatedAt()).isNull();
    }
}
