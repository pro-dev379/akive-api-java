package net.akive.api.plan.apapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.plan.domain.WeekPeriod;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class WeekPeriodEntity {

    @Column(name = "week_start_on", nullable = false)
    private LocalDate startOn;

    @Column(name = "week_end_on", nullable = false)
    private LocalDate endOn;

    public static WeekPeriodEntity from(WeekPeriod weekPeriod) {
        return new WeekPeriodEntity(weekPeriod.startOn(), weekPeriod.endOn());
    }

    public WeekPeriod to() {
        return new WeekPeriod(this.startOn, this.endOn);
    }
}
