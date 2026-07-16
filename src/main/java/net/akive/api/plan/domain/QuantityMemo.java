package net.akive.api.plan.domain;

import static net.akive.api.common.domain.DomainValidator.requireMaxLengthIfPresent;

public record QuantityMemo(String value) {

    public QuantityMemo {
        requireMaxLengthIfPresent(value, 25);
    }
}