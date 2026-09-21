package net.akive.api.dailylog.domain;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class DailyLogPolicy {

    public static final int DAY_START_HOUR = 5;
    public static final long MIN_SLEEP_DURATION_SEC = 2 * 60 * 60;
}
