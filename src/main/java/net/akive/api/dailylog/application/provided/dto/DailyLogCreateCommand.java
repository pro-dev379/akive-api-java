package net.akive.api.dailylog.application.provided.dto;

import net.akive.api.dailylog.domain.DailyLogType;
import net.akive.api.dailylog.domain.FocusTime;

import java.time.LocalDateTime;

import static net.akive.api.common.domain.DomainValidator.*;

public record DailyLogCreateCommand(
        Long userId,
        FocusTime focusTime,
        DailyLogType dailyLogType,
        String contents,
        LocalDateTime requestedAt
) {

    public DailyLogCreateCommand {
        requirePositive(userId);
        requireNonNull(focusTime);
        requireNonNull(dailyLogType);
        requireNotBlank(contents);
        requireNonNull(requestedAt);
    }
}
