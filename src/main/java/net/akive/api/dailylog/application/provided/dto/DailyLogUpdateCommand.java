package net.akive.api.dailylog.application.provided.dto;

import net.akive.api.dailylog.domain.DailyLogType;
import net.akive.api.dailylog.domain.FocusTime;

import java.time.LocalDateTime;

import static net.akive.api.common.domain.DomainValidator.*;

public record DailyLogUpdateCommand(
        Long dailyLogId,
        Long userId,
        FocusTime targetFocusTime,
        DailyLogType targetDailyLogType,
        String targetContents,
        LocalDateTime requestedAt
) {

    public DailyLogUpdateCommand {
        requirePositive(dailyLogId);
        requirePositive(userId);
        requireNonNull(targetFocusTime);
        requireNonNull(targetDailyLogType);
        requireNotBlank(targetContents);
        requireNonNull(requestedAt);
    }
}
