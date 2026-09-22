package net.akive.api.dailylog.application.provided.dto;

import net.akive.api.dailylog.domain.ActivityTime;
import net.akive.api.dailylog.domain.DailyLogType;

import java.time.LocalDateTime;

import static net.akive.api.common.domain.DomainValidator.*;

public record DailyLogCreateCommand(
        Long userId,
        ActivityTime activityTime,
        DailyLogType dailyLogType,
        String contents,
        LocalDateTime requestedAt
) {

    public DailyLogCreateCommand {
        requirePositive(userId);
        requireNonNull(activityTime);
        requireNonNull(dailyLogType);
        requireNotBlank(contents);
        requireNonNull(requestedAt);
    }
}
