package net.akive.api.plan.adapter.webapi.common.dto.response;

import net.akive.api.plan.domain.value.Subject;

public record SubjectResponse(String name, String category) {

    public static SubjectResponse from(Subject subject) {
        return new SubjectResponse(subject.name(), subject.category());
    }
}
