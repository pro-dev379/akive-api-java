package net.akive.api.dailylog.domain;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.domain.GoalVersion;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;
import static net.akive.api.common.domain.DomainValidator.requireStartNotAfterEnd;
import static net.akive.api.common.exception.business.ResourceNotFoundError.GOAL_NOT_FOUND;
import static net.akive.api.dailylog.domain.DailyLogPolicy.DAILY_BOUNDARY_HOUR;
import static net.akive.api.dailylog.domain.DailyLogType.*;

public record FocusMetrics(long focusTimeSec, BigDecimal focusRate) {

    private static final ZoneId GOAL_DATE_ZONE = ZoneId.of("Asia/Seoul");

    public static FocusMetrics calculate(
            LocalDate startOn,
            LocalDate endOn,
            List<DailyLog> dailyLogs,
            List<GoalVersion> goals
    ) {
        requireStartNotAfterEnd(startOn, endOn);

        BigDecimal totalFocusSeconds = BigDecimal.ZERO;
        BigDecimal totalRequiredFocusSeconds = BigDecimal.ZERO;

        for (LocalDate date = startOn; !date.isAfter(endOn); date = date.plusDays(1)) {
            DailyFocusBasis dailyFocusBasis = calculateDay(date, dailyLogs, goals);
            totalFocusSeconds = totalFocusSeconds.add(dailyFocusBasis.focusSeconds());
            totalRequiredFocusSeconds = totalRequiredFocusSeconds.add(dailyFocusBasis.requiredFocusSeconds());
        }

        BigDecimal focusRate = totalRequiredFocusSeconds.signum() == 0
                ? (totalFocusSeconds.signum() == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100))
                : totalFocusSeconds.multiply(BigDecimal.valueOf(100))
                        .divide(totalRequiredFocusSeconds, 1, HALF_UP)
                        .max(BigDecimal.ZERO)
                        .min(BigDecimal.valueOf(100));

        return new FocusMetrics(
                totalFocusSeconds.setScale(0, HALF_UP).longValueExact(),
                focusRate.setScale(1, HALF_UP)
        );
    }

    private static DailyFocusBasis calculateDay(
            LocalDate date,
            List<DailyLog> dailyLogs,
            List<GoalVersion> goals
    ) {
        LocalDateTime dayStart = date.atTime(DAILY_BOUNDARY_HOUR, 0);
        LocalDateTime dayEnd = dayStart.plusDays(1);

        LocalDateTime firstStudyStart = null;
        LocalDateTime lastStudyEnd = null;
        BigDecimal focusSeconds = BigDecimal.ZERO;

        for (DailyLog dailyLog : dailyLogs) {
            if (dailyLog.getDailyLogType() != STUDY) continue;

            LocalDateTime overlapStart = latest(dailyLog.getStartOn(), dayStart);
            LocalDateTime overlapEnd = earliest(dailyLog.getEndOn(), dayEnd);

            if (!overlapStart.isBefore(overlapEnd)) continue;

            firstStudyStart = firstStudyStart == null ? overlapStart : earliest(firstStudyStart, overlapStart);
            lastStudyEnd = lastStudyEnd == null ? overlapEnd : latest(lastStudyEnd, overlapEnd);

            if (dailyLog.getFocusTimeSec() != null) {
                focusSeconds = focusSeconds.add(proportionalFocusSeconds(dailyLog, overlapStart, overlapEnd));
            }
        }

        if (firstStudyStart == null) return new DailyFocusBasis(BigDecimal.ZERO, BigDecimal.ZERO);

        GoalVersion goal = findEffectiveGoalOn(date, goals);

        long mealSeconds = 0;
        long restSeconds = 0;
        long excludedSeconds = 0;

        for (DailyLog dailyLog : dailyLogs) {
            LocalDateTime overlapStart = latest(dailyLog.getStartOn(), firstStudyStart);
            LocalDateTime overlapEnd = earliest(dailyLog.getEndOn(), lastStudyEnd);

            if (!overlapStart.isBefore(overlapEnd)) continue;

            long seconds = Duration.between(overlapStart, overlapEnd).toSeconds();

            if (dailyLog.getDailyLogType() == MEAL) mealSeconds += seconds;
            if (dailyLog.getDailyLogType() == REST) restSeconds += seconds;
            if (dailyLog.getDailyLogType() == SLEEP
                    || (dailyLog.getDailyLogType() == ETC && dailyLog.isFocusRateExcluded())) {
                excludedSeconds += seconds;
            }
        }

        BigDecimal allowedMealSeconds = BigDecimal.valueOf(mealSeconds)
                        .min(BigDecimal.valueOf(goal.meal1TimeSec())
                        .multiply(BigDecimal.valueOf(goal.mealNum())));
        BigDecimal allowedRestSeconds = BigDecimal.valueOf(restSeconds)
                .min(focusSeconds.multiply(BigDecimal.valueOf(goal.rest10MinSec()))
                .divide(BigDecimal.valueOf(600), 12, HALF_UP));
        BigDecimal requiredSeconds = BigDecimal.valueOf(Duration.between(firstStudyStart, lastStudyEnd).toSeconds())
                .subtract(allowedMealSeconds)
                .subtract(allowedRestSeconds)
                .subtract(BigDecimal.valueOf(excludedSeconds))
                .max(BigDecimal.ZERO);

        return new DailyFocusBasis(focusSeconds, requiredSeconds);
    }

    private static GoalVersion findEffectiveGoalOn(LocalDate date, List<GoalVersion> goals) {
        GoalVersion latest = null;

        for (GoalVersion goal : goals) {
            LocalDate effectiveOn =
                    goal.createdAt().atOffset(ZoneOffset.UTC).atZoneSameInstant(GOAL_DATE_ZONE).toLocalDate();

            if (!effectiveOn.isAfter(date)
                    && (latest == null || !goal.createdAt().isBefore(latest.createdAt()))) {
                latest = goal;
            }
        }

        if (latest == null) throw new ResourceNotFoundException(GOAL_NOT_FOUND);

        return latest;
    }

    private static BigDecimal proportionalFocusSeconds(
            DailyLog dailyLog,
            LocalDateTime overlapStart,
            LocalDateTime overlapEnd
    ) {
        long durationNanos = Duration.between(dailyLog.getStartOn(), dailyLog.getEndOn()).toNanos();
        long overlapNanos = Duration.between(overlapStart, overlapEnd).toNanos();

        return BigDecimal.valueOf(dailyLog.getFocusTimeSec())
                .multiply(BigDecimal.valueOf(overlapNanos))
                .divide(BigDecimal.valueOf(durationNanos), 12, HALF_UP);
    }

    private static LocalDateTime latest(LocalDateTime first, LocalDateTime second) {
        return first.isAfter(second) ? first : second;
    }

    private static LocalDateTime earliest(LocalDateTime first, LocalDateTime second) {
        return first.isBefore(second) ? first : second;
    }

    private record DailyFocusBasis(BigDecimal focusSeconds, BigDecimal requiredFocusSeconds) {
    }
}
