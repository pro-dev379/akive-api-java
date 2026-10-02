package net.akive.api.dailylog.application.provided.dto;

import net.akive.api.dailylog.domain.ActivityTime;

import java.time.Instant;
import java.time.LocalDateTime;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record ActivityTimeCreateCommand(
        LocalDateTime startOn,
        LocalDateTime endOn,
        Long focusTimeSec,
        boolean focusRateExcluded
) {

    public ActivityTimeCreateCommand {
        requireNonNull(startOn);
        requireNonNull(endOn);
    }

    public ActivityTime to(Instant baseTime) {
        return ActivityTime.of(startOn, endOn, focusTimeSec, focusRateExcluded, baseTime);
    }
}
