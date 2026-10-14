package net.akive.api.llmusage.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.llmusage.application.provided.LlmUsageRecorder;
import net.akive.api.llmusage.application.provided.dto.LlmUsageRecordCommand;
import net.akive.api.llmusage.application.required.LlmUsageLogRepository;
import net.akive.api.llmusage.domain.LlmUsageLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
class LlmUsageRecordService implements LlmUsageRecorder {

    private final LlmUsageLogRepository llmUsageLogRepository;

    @Override
    public void record(LlmUsageRecordCommand command) {
        llmUsageLogRepository.save(LlmUsageLog.create(
                command.userId(),
                command.purpose(),
                command.provider(),
                command.providerResponseId(),
                command.modelName(),
                command.usageScope(),
                command.resultStatus(),
                command.inputTokenCount(),
                command.cachedInputTokenCount(),
                command.outputTokenCount(),
                command.reasoningOutputTokenCount()
        ));
    }
}
