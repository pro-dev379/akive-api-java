package net.akive.api.plan.domain.value;

import static net.akive.api.common.domain.DomainValidator.requireMaxLengthIfPresent;
import static net.akive.api.plan.domain.policy.PlanPolicy.QUANTITY_MEMO_MAX_LENGTH;

public record QuantityMemo(String value) {

    public static final QuantityMemo NONE = new QuantityMemo(null);

    public QuantityMemo {
        requireMaxLengthIfPresent(value, QUANTITY_MEMO_MAX_LENGTH);
    }
}
