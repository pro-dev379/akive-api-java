package net.akive.api.dailylog.application.provided.dto;

import net.akive.api.dailylog.domain.DailyLogType;

import static net.akive.api.common.domain.DomainValidator.*;

public record DailyLogCreateCommand(
        Long userId,
        ActivityTimeCreateCommand activityTime,
        DailyLogType dailyLogType,
        String contents
) {

    public DailyLogCreateCommand {
        requirePositive(userId);
        requireNonNull(activityTime);
        requireNonNull(dailyLogType);
        requireNotBlank(contents);
    }
}
