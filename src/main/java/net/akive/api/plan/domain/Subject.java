package net.akive.api.plan.domain;

import static net.akive.api.common.domain.DomainValidator.requireMaxLength;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record Subject(String name, String category) {

    public Subject {
        name = requireMaxLength(requireNotBlank(name), 100);
        category = requireMaxLength(requireNotBlank(category), 10);
    }
}