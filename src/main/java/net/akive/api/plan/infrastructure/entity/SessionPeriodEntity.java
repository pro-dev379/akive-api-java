package net.akive.api.plan.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.plan.domain.SessionPeriod;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class SessionPeriodEntity {

    @Column(name = "session_start_on", nullable = false)
    private LocalDate startOn;

    @Column(name = "session_end_on", nullable = false)
    private LocalDate endOn;

    public static SessionPeriodEntity from(SessionPeriod sessionPeriod) {
        return new SessionPeriodEntity(sessionPeriod.startOn(), sessionPeriod.endOn());
    }

    public SessionPeriod to() {
        return new SessionPeriod(this.startOn, this.endOn);
    }
}
