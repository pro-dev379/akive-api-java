package net.akive.api.dailylog.application.provided.dto;

import net.akive.api.dailylog.domain.DailyLogType;

import static net.akive.api.common.domain.DomainValidator.*;

public record DailyLogUpdateCommand(
        Long dailyLogId,
        Long userId,
        ActivityTimeCreateCommand targetActivityTime,
        DailyLogType targetDailyLogType,
        String targetContents
) {

    public DailyLogUpdateCommand {
        requirePositive(dailyLogId);
        requirePositive(userId);
        requireNonNull(targetActivityTime);
        requireNonNull(targetDailyLogType);
        requireNotBlank(targetContents);
    }
}
