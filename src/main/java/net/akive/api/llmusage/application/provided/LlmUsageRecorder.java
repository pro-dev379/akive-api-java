package net.akive.api.llmusage.application.provided;

import net.akive.api.llmusage.application.provided.dto.LlmUsageRecordCommand;

public interface LlmUsageRecorder {

    void record(LlmUsageRecordCommand command);
}
