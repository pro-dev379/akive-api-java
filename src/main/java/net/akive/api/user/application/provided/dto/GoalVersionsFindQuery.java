package net.akive.api.user.application.provided.dto;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.requirePositive;
import static net.akive.api.common.domain.DomainValidator.requireStartNotAfterEnd;

public record GoalVersionsFindQuery(Long userId, LocalDate startOn, LocalDate endOn) {

    public GoalVersionsFindQuery {
        requirePositive(userId);
        requireStartNotAfterEnd(startOn, endOn);
    }
}
