package net.akive.api.dailylog.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.DailyLogType;
import net.akive.api.user.adapter.persistence.entity.UserEntity;

import java.time.Instant;
import java.time.LocalDate;

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
    private ActivityTimeEntity activityTime;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private DailyLogType dailyLogType;

    @Column(nullable = false)
    private String contents;

    public static DailyLogEntity from(DailyLog dailyLog) {
        return new DailyLogEntity(
                dailyLog.getDailyLogId(),
                UserEntity.from(dailyLog.getUser()),
                dailyLog.getLogOn(),
                ActivityTimeEntity.from(dailyLog.getActivityTime()),
                dailyLog.getDailyLogType(),
                dailyLog.getContents()
        );
    }

    public DailyLog to(Instant reconstructedAt) {
        try {
            return DailyLog.reconstruct(
                    this.dailyLogId,
                    this.user.to(),
                    this.logOn,
                    this.activityTime.to(reconstructedAt),
                    this.dailyLogType,
                    this.contents,
                    reconstructedAt
            );
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException("Failed to reconstruct DailyLog: " + this.dailyLogId, e);
        }
    }
}
