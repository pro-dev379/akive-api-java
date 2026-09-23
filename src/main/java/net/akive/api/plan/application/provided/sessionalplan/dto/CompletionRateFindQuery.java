package net.akive.api.plan.application.provided.sessionalplan.dto;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.requirePositive;
import static net.akive.api.common.domain.DomainValidator.requireStartNotAfterEnd;

public record CompletionRateFindQuery(Long userId, LocalDate startOn, LocalDate endOn) {

    public CompletionRateFindQuery {
        requirePositive(userId);
        requireStartNotAfterEnd(startOn, endOn);
    }
}
