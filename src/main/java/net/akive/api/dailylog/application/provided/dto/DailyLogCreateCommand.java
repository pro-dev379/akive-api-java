package net.akive.api.dailylog.application.provided.dto;

import net.akive.api.dailylog.domain.DailyLogType;
import net.akive.api.dailylog.domain.DailyLogTimeRange;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.*;

public record DailyLogCreateCommand(
        Long userId,
        LocalDate logOn,
        DailyLogTimeRange timeRange,
        DailyLogType dailyLogType,
        Long focusTimeSec,
        boolean focusRateExcluded,
        String contents
) {

    public DailyLogCreateCommand {
        requirePositive(userId);
        requireNonNull(logOn);
        requireNonNull(timeRange);
        requireNonNull(dailyLogType);
        requireNonNegativeIfPresent(focusTimeSec);
        requireNotBlank(contents);
    }
}
