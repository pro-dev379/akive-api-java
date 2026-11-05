package net.akive.api.common.cache;

import net.akive.api.dailylog.adapter.webapi.dto.response.DailyLogTypesFindResponse;
import net.akive.api.support.RedisTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.cache.CacheKeyProvider.*;
import static net.akive.api.user.domain.fixture.UserFixture.ANOTHER_USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

class ResponseCacheTest extends RedisTestSupport {

    @Autowired
    ResponseCache responseCache;

    @Test
    void 응답_DTO를_저장하고_조회한다() {
        String key = dailyLogTypes();
        DailyLogTypesFindResponse response = new DailyLogTypesFindResponse(List.of(
                new DailyLogTypesFindResponse.DailyLogTypeResponse("STUDY", "공부")
        ));

        responseCache.put(key, response, Duration.ofMinutes(1));

        assertThat(responseCache.get(key, DailyLogTypesFindResponse.class)).contains(response);
    }

    @Test
    void 주어진_캐시_키_패턴에_해당하는_캐시만_삭제한다() {
        LocalDate startOn = LocalDate.of(2026, 10, 1);
        LocalDate endOn = LocalDate.of(2026, 10, 31);
        String evictedCacheKey = dailyLogFocusMetrics(USER_ID, startOn, endOn);
        String retainedCacheKey = dailyLogFocusMetrics(ANOTHER_USER_ID, startOn, endOn);
        DailyLogTypesFindResponse response = new DailyLogTypesFindResponse(List.of());
        responseCache.put(evictedCacheKey, response, Duration.ofMinutes(1));
        responseCache.put(retainedCacheKey, response, Duration.ofMinutes(1));

        responseCache.evictByPattern(dailyLogAnalyticsPattern(USER_ID));

        assertThat(responseCache.get(evictedCacheKey, DailyLogTypesFindResponse.class)).isEmpty();
        assertThat(responseCache.get(retainedCacheKey, DailyLogTypesFindResponse.class)).contains(response);
    }
}
