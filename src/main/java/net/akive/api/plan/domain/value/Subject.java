package net.akive.api.plan.domain.value;

import static net.akive.api.common.domain.DomainValidator.requireMaxLength;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.plan.domain.policy.PlanPolicy.SUBJECT_CATEGORY_MAX_LENGTH;
import static net.akive.api.plan.domain.policy.PlanPolicy.SUBJECT_NAME_MAX_LENGTH;

public record Subject(String name, String category) {

    public Subject {
        name = requireMaxLength(requireNotBlank(name), SUBJECT_NAME_MAX_LENGTH);
        category = requireMaxLength(requireNotBlank(category), SUBJECT_CATEGORY_MAX_LENGTH);
    }
}
