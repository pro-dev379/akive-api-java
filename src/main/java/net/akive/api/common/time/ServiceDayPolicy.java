package net.akive.api.common.time;

import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ServiceDayPolicy {

    public static final int DAILY_BOUNDARY_HOUR = 5;
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    public static LocalDate toServiceDate(Instant instant) {
        return Objects.requireNonNull(instant)
                .atZone(SERVICE_ZONE)
                .minusHours(DAILY_BOUNDARY_HOUR)
                .toLocalDate();
    }

    public static LocalDate toKoreanDate(Instant instant) {
        return Objects.requireNonNull(instant)
                .atZone(SERVICE_ZONE)
                .toLocalDate();
    }

    public static Instant toInstantAtServiceZone(LocalDateTime dateTime) {
        return Objects.requireNonNull(dateTime)
                .atZone(SERVICE_ZONE)
                .toInstant();
    }

    public static Instant toServiceDayStart(LocalDate serviceDate) {
        return Objects.requireNonNull(serviceDate)
                .atTime(DAILY_BOUNDARY_HOUR, 0)
                .atZone(SERVICE_ZONE)
                .toInstant();
    }
}
