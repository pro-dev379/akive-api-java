package net.akive.api.plan.domain;

import static net.akive.api.common.domain.DomainValidator.requireMaxLengthIfPresent;
import static net.akive.api.plan.domain.PlanPolicy.QUANTITY_MEMO_MAX_LENGTH;

public record QuantityMemo(String value) {

    public QuantityMemo {
        requireMaxLengthIfPresent(value, QUANTITY_MEMO_MAX_LENGTH);
    }
}