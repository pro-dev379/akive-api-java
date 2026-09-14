package net.akive.api.dailylog.application.provided.dto;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.dailylog.domain.DailyLogType;
import net.akive.api.dailylog.domain.FocusTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.common.domain.DomainValidator.requirePositive;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.DAILY_LOG_DATE_IN_FUTURE;

public record DailyLogCreateCommand(
        Long userId,
        LocalDate logOn,
        FocusTime focusTime,
        DailyLogType dailyLogType,
        String contents,
        LocalDateTime requestedAt
) {

    public DailyLogCreateCommand {
        requirePositive(userId);
        requireNonNull(logOn);
        requireNonNull(focusTime);
        requireNonNull(dailyLogType);
        requireNotBlank(contents);
        requireNonNull(requestedAt);

        if (logOn.isAfter(requestedAt.toLocalDate())) {
            throw new BusinessRuleViolationException(DAILY_LOG_DATE_IN_FUTURE);
        }
    }
}
