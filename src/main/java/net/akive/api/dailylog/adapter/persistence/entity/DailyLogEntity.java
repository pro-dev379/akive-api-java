package net.akive.api.dailylog.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.DailyLogType;
import net.akive.api.user.adapter.persistence.entity.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "daily_log",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-daily_log-user_id-log_on-start_on",
                        columnNames = {"user_id", "log_on", "start_on"}
                ),
                @UniqueConstraint(
                        name = "ux-daily_log-user_id-log_on-end_on",
                        columnNames = {"user_id", "log_on", "end_on"}
                ),
        }
)
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class DailyLogEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long dailyLogId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalDate logOn;

    @Embedded
    private DailyLogTimeRangeEntity timeRange;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private DailyLogType dailyLogType;

    private Long focusTimeSec;

    @Column(
            name = "is_focus_rate_excluded",
            nullable = false,
            columnDefinition = "TINYINT(1) UNSIGNED NOT NULL DEFAULT 0"
    )
    private byte focusRateExcluded = 0;

    @Column(nullable = false)
    private String contents;

    public static DailyLogEntity from(DailyLog dailyLog) {
        return new DailyLogEntity(
                dailyLog.getDailyLogId(),
                UserEntity.from(dailyLog.getUser()),
                dailyLog.getLogOn(),
                DailyLogTimeRangeEntity.from(dailyLog.getTimeRange()),
                dailyLog.getDailyLogType(),
                dailyLog.getFocusTimeSec(),
                focusRateExcludedValue(dailyLog.isFocusRateExcluded()),
                dailyLog.getContents()
        );
    }

    public DailyLog to(LocalDateTime now) {
        return DailyLog.reconstruct(
                this.dailyLogId,
                this.user.to(),
                this.logOn,
                this.timeRange.to(now),
                this.dailyLogType,
                this.focusTimeSec,
                this.isFocusRateExcluded(),
                this.contents
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
