package net.akive.api.plan.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.plan.domain.Subject;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class SubjectEntity {

    @Column(name = "subject", length = 100, nullable = false)
    private String name;

    @Column(name = "subject_category", length = 10, nullable = false)
    private String category;

    public static SubjectEntity from(Subject subject) {
        return new SubjectEntity(subject.name(), subject.category());
    }

    public Subject to() {
        return new Subject(this.name, this.category);
    }
}