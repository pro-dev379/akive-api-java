package net.akive.api.dailylog.domain;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.math.RoundingMode.HALF_UP;
import static net.akive.api.common.domain.DomainValidator.requireStartNotAfterEnd;
import static net.akive.api.dailylog.domain.DailyLogPolicy.DAILY_BOUNDARY_HOUR;

public record TimeUsageRates(
        Map<DailyLogType, BigDecimal> ratesByType,
        BigDecimal unrecordedRate
) {

    public static TimeUsageRates calculate(LocalDate startOn, LocalDate endOn, List<DailyLog> dailyLogs) {
        requireStartNotAfterEnd(startOn, endOn);
        LocalDateTime periodStart = startOn.atTime(DAILY_BOUNDARY_HOUR, 0);
        LocalDateTime periodEnd = endOn.plusDays(1).atTime(DAILY_BOUNDARY_HOUR, 0);
        long totalSeconds = Duration.between(periodStart, periodEnd).toSeconds();
        Map<DailyLogType, Long> secondsByType = new EnumMap<>(DailyLogType.class);

        for (DailyLog dailyLog : dailyLogs) {
            LocalDateTime overlapStart = dailyLog.getStartOn().isAfter(periodStart)
                    ? dailyLog.getStartOn() : periodStart;
            LocalDateTime overlapEnd = dailyLog.getEndOn().isBefore(periodEnd)
                    ? dailyLog.getEndOn() : periodEnd;

            if (overlapStart.isBefore(overlapEnd)) {
                long seconds = Duration.between(overlapStart, overlapEnd).toSeconds();
                secondsByType.merge(dailyLog.getDailyLogType(), seconds, Long::sum);
            }
        }

        long recordedSeconds = secondsByType.values().stream().mapToLong(Long::longValue).sum();
        if (recordedSeconds > totalSeconds) {
            throw new IllegalStateException("Daily log time exceeds the requested period");
        }

        Map<DailyLogType, BigDecimal> ratesByType = new EnumMap<>(DailyLogType.class);
        for (DailyLogType type : DailyLogType.values()) {
            ratesByType.put(type, toRate(secondsByType.getOrDefault(type, 0L), totalSeconds));
        }

        return new TimeUsageRates(
                Map.copyOf(ratesByType),
                toRate(totalSeconds - recordedSeconds, totalSeconds)
        );
    }

    private static BigDecimal toRate(long seconds, long totalSeconds) {
        return BigDecimal.valueOf(seconds)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalSeconds), 1, HALF_UP);
    }
}
