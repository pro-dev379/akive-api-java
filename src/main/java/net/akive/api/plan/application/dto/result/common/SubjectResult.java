package net.akive.api.plan.application.dto.result.common;

import net.akive.api.plan.domain.Subject;

public record SubjectResult(String name, String category) {

    public static SubjectResult from(Subject subject) {
        return new SubjectResult(subject.name(), subject.category());
    }
}