package net.akive.api.dailylog.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.dailylog.domain.DailyLogTimeRange;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class DailyLogTimeRangeEntity {

    @Column(nullable = false)
    private LocalDateTime startOn;

    @Column(nullable = false)
    private LocalDateTime endOn;

    public static DailyLogTimeRangeEntity from(DailyLogTimeRange timeRange) {
        return new DailyLogTimeRangeEntity(timeRange.getStartOn(), timeRange.getEndOn());
    }

    public DailyLogTimeRange to(LocalDateTime now) {
        return DailyLogTimeRange.of(this.startOn, this.endOn, now);
    }
}
