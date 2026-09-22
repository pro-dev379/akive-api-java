package net.akive.api.dailylog.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.dailylog.domain.ActivityTime;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class ActivityTimeEntity {

    @Column(nullable = false)
    private LocalDateTime startOn;

    @Column(nullable = false)
    private LocalDateTime endOn;

    private Long focusTimeSec;

    @Column(
            name = "is_focus_rate_excluded",
            nullable = false,
            columnDefinition = "TINYINT(1) UNSIGNED NOT NULL DEFAULT 0"
    )
    private byte focusRateExcluded = 0;

    public static ActivityTimeEntity from(ActivityTime activityTime) {
        return new ActivityTimeEntity(
                activityTime.getStartOn(),
                activityTime.getEndOn(),
                activityTime.getFocusTimeSec(),
                focusRateExcludedValue(activityTime.isFocusRateExcluded())
        );
    }

    public ActivityTime to(LocalDateTime reconstructedAt) {
        return ActivityTime.of(
                this.startOn,
                this.endOn,
                this.focusTimeSec,
                this.isFocusRateExcluded(),
                reconstructedAt
        );
    }

    private boolean isFocusRateExcluded() {
        if (this.focusRateExcluded != 0 && this.focusRateExcluded != 1) {
            throw new IllegalStateException("Invalid focusRateExcluded value: " + this.focusRateExcluded);
        }

        return this.focusRateExcluded == 1;
    }

    private static byte focusRateExcludedValue(boolean focusRateExcluded) {
        return focusRateExcluded ? (byte) 1 : (byte) 0;
    }
}
