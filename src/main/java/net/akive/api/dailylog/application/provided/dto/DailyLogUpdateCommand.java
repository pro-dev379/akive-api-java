package net.akive.api.dailylog.application.provided.dto;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.dailylog.domain.DailyLogType;
import net.akive.api.dailylog.domain.FocusTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.DAILY_LOG_DATE_IN_FUTURE;

public record DailyLogUpdateCommand(
        Long dailyLogId,
        Long userId,
        LocalDate targetLogOn,
        FocusTime targetFocusTime,
        DailyLogType targetDailyLogType,
        String targetContents,
        LocalDateTime requestedAt
) {

    public DailyLogUpdateCommand {
        requirePositive(dailyLogId);
        requirePositive(userId);
        requireNonNull(targetLogOn);
        requireNonNull(targetFocusTime);
        requireNonNull(targetDailyLogType);
        requireNotBlank(targetContents);
        requireNonNull(requestedAt);

        if (targetLogOn.isAfter(requestedAt.toLocalDate())) {
            throw new BusinessRuleViolationException(DAILY_LOG_DATE_IN_FUTURE);
        }
    }
}
