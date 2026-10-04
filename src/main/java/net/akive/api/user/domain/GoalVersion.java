package net.akive.api.user.domain;

import java.time.Instant;

import static net.akive.api.common.domain.DomainValidator.requireNonNegative;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record GoalVersion(
        long rest10MinSec,
        int mealNum,
        long meal1TimeSec,
        Instant createdAt
) {

    public GoalVersion {
        requireNonNegative(rest10MinSec);
        requireNonNegative(mealNum);
        requireNonNegative(meal1TimeSec);
        requireNonNull(createdAt);
    }
}
