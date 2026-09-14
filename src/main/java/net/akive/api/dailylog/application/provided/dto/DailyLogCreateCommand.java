package net.akive.api.dailylog.application.provided.dto;

import net.akive.api.dailylog.domain.DailyLogType;
import net.akive.api.dailylog.domain.FocusTime;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record DailyLogCreateCommand(
        Long userId,
        FocusTime focusTime,
        DailyLogType dailyLogType,
        String contents
) {

    public DailyLogCreateCommand {
        requirePositive(userId);
        requireNonNull(focusTime);
        requireNonNull(dailyLogType);
        requireNotBlank(contents);
    }
}
