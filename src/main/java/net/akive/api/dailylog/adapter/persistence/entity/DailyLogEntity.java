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

    @Column(nullable = false)
    private LocalDateTime startOn;

    @Column(nullable = false)
    private LocalDateTime endOn;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private DailyLogType dailyLogType;

    private Long intensiveTimeSec;

    @Column(
            name = "is_intensive_excluded",
            nullable = false,
            columnDefinition = "TINYINT(1) UNSIGNED NOT NULL DEFAULT 0"
    )
    private byte intensiveExcluded = 0;

    @Column(nullable = false)
    private String contents;

    public static DailyLogEntity from(DailyLog dailyLog) {
        return new DailyLogEntity(
                dailyLog.getDailyLogId(),
                UserEntity.from(dailyLog.getUser()),
                dailyLog.getLogOn(),
                dailyLog.getStartOn(),
                dailyLog.getEndOn(),
                dailyLog.getDailyLogType(),
                dailyLog.getIntensiveTimeSec(),
                intensiveExcludedValue(dailyLog.isIntensiveExcluded()),
                dailyLog.getContents()
        );
    }

    public DailyLog to() {
        return DailyLog.reconstruct(
                this.dailyLogId,
                this.user.to(),
                this.logOn,
                this.startOn,
                this.endOn,
                this.dailyLogType,
                this.intensiveTimeSec,
                this.isIntensiveExcluded(),
                this.contents
        );
    }

    private boolean isIntensiveExcluded() {
        if (intensiveExcluded != 0 && intensiveExcluded != 1) {
            throw new IllegalStateException("Invalid intensiveExcluded value: " + intensiveExcluded);
        }

        return intensiveExcluded == 1;
    }

    private static byte intensiveExcludedValue(boolean intensiveExcluded) {
        return intensiveExcluded ? (byte) 1 : (byte) 0;
    }
}
