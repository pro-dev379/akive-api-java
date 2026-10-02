package net.akive.api.dailylog.application.provided.dto;

import net.akive.api.dailylog.domain.ActivityTime;
import net.akive.api.dailylog.domain.DailyLogType;

import java.time.Instant;

import static net.akive.api.common.domain.DomainValidator.*;

public record DailyLogUpdateCommand(
        Long dailyLogId,
        Long userId,
        ActivityTime targetActivityTime,
        DailyLogType targetDailyLogType,
        String targetContents,
        Instant requestedAt
) {

    public DailyLogUpdateCommand {
        requirePositive(dailyLogId);
        requirePositive(userId);
        requireNonNull(targetActivityTime);
        requireNonNull(targetDailyLogType);
        requireNotBlank(targetContents);
        requireNonNull(requestedAt);
    }
}
