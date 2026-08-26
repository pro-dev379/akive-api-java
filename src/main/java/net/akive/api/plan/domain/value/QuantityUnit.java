package net.akive.api.plan.domain.value;

import static net.akive.api.common.domain.DomainValidator.requireMaxLength;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.plan.domain.policy.PlanPolicy.QUANTITY_UNIT_MAX_LENGTH;

public record QuantityUnit(String value) {

    public QuantityUnit {
        value = requireMaxLength(requireNotBlank(value), QUANTITY_UNIT_MAX_LENGTH);
    }
}
