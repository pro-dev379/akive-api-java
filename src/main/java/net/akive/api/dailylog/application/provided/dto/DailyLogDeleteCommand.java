package net.akive.api.dailylog.application.provided.dto;

import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record DailyLogDeleteCommand(Long dailyLogId, Long userId) {

    public DailyLogDeleteCommand {
        requirePositive(dailyLogId);
        requirePositive(userId);
    }
}
