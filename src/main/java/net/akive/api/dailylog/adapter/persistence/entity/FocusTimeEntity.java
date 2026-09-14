package net.akive.api.dailylog.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.dailylog.domain.FocusTime;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class FocusTimeEntity {

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

    public static FocusTimeEntity from(FocusTime focusTime) {
        return new FocusTimeEntity(
                focusTime.getStartOn(),
                focusTime.getEndOn(),
                focusTime.getFocusTimeSec(),
                focusRateExcludedValue(focusTime.isFocusRateExcluded())
        );
    }

    public FocusTime to(LocalDateTime now) {
        return FocusTime.of(
                this.startOn,
                this.endOn,
                this.focusTimeSec,
                this.isFocusRateExcluded(),
                now
        );
    }

    private boolean isFocusRateExcluded() {
        if (focusRateExcluded != 0 && focusRateExcluded != 1) {
            throw new IllegalStateException("Invalid focusRateExcluded value: " + focusRateExcluded);
        }

        return focusRateExcluded == 1;
    }

    private static byte focusRateExcludedValue(boolean focusRateExcluded) {
        return focusRateExcluded ? (byte) 1 : (byte) 0;
    }
}
