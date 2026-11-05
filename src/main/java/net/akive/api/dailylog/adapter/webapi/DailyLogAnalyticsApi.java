package net.akive.api.dailylog.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.cache.ResponseCache;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.dailylog.adapter.webapi.dto.response.FocusMetricsFindResponse;
import net.akive.api.dailylog.adapter.webapi.dto.response.FocusMetricsSeriesFindResponse;
import net.akive.api.dailylog.adapter.webapi.dto.response.TimeUsageRatesFindResponse;
import net.akive.api.dailylog.application.provided.DailyLogFinder;
import net.akive.api.dailylog.application.provided.dto.FocusMetricsFindQuery;
import net.akive.api.dailylog.application.provided.dto.FocusMetricsSeriesFindQuery;
import net.akive.api.dailylog.application.provided.dto.TimeUsageRatesFindQuery;
import net.akive.api.common.analytics.TimeSeriesUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static net.akive.api.common.cache.CacheKeyProvider.dailyLogFocusMetrics;
import static net.akive.api.common.cache.CacheKeyProvider.dailyLogFocusSeries;
import static net.akive.api.common.cache.CacheKeyProvider.dailyLogTimeUsageRates;
import static net.akive.api.common.cache.ResponseCacheTtl.ANALYTICS;

@RestController
@RequestMapping("/daily-logs/analytics")
@RequiredArgsConstructor
public class DailyLogAnalyticsApi {

    private final DailyLogFinder dailyLogFinder;
    private final ResponseCache responseCache;

    @GetMapping("/analytics/time-usage-rates")
    public TimeUsageRatesFindResponse findTimeUsageRates(
            @AuthenticatedUserId Long userId,
            @RequestParam LocalDate startOn,
            @RequestParam LocalDate endOn
    ) {
        TimeUsageRatesFindQuery query = new TimeUsageRatesFindQuery(userId, startOn, endOn);
        String cacheKey = dailyLogTimeUsageRates(userId, startOn, endOn);

        return responseCache.get(cacheKey, TimeUsageRatesFindResponse.class)
                .orElseGet(() -> {
                    TimeUsageRatesFindResponse response =
                            TimeUsageRatesFindResponse.from(dailyLogFinder.findTimeUsageRates(query));
                    responseCache.put(cacheKey, response, ANALYTICS);

                    return response;
                });
    }

    @GetMapping("/focus/metrics")
    public FocusMetricsFindResponse findFocusMetrics(
            @AuthenticatedUserId Long userId,
            @RequestParam LocalDate startOn,
            @RequestParam LocalDate endOn
    ) {
        FocusMetricsFindQuery query = new FocusMetricsFindQuery(userId, startOn, endOn);
        String cacheKey = dailyLogFocusMetrics(userId, startOn, endOn);

        return responseCache.get(cacheKey, FocusMetricsFindResponse.class)
                .orElseGet(() -> {
                    FocusMetricsFindResponse response =
                            FocusMetricsFindResponse.from(dailyLogFinder.findFocusMetrics(query));
                    responseCache.put(cacheKey, response, ANALYTICS);

                    return response;
                });
    }

    @GetMapping("/focus/series")
    public FocusMetricsSeriesFindResponse findFocusMetricsSeries(
            @AuthenticatedUserId Long userId,
            @RequestParam LocalDate startOn,
            @RequestParam LocalDate endOn,
            @RequestParam TimeSeriesUnit unit
    ) {
        FocusMetricsSeriesFindQuery query = new FocusMetricsSeriesFindQuery(userId, startOn, endOn, unit);
        String cacheKey = dailyLogFocusSeries(userId, startOn, endOn, unit);

        return responseCache.get(cacheKey, FocusMetricsSeriesFindResponse.class)
                .orElseGet(() -> {
                    FocusMetricsSeriesFindResponse response =
                            FocusMetricsSeriesFindResponse.from(dailyLogFinder.findFocusMetricsSeries(query));
                    responseCache.put(cacheKey, response, ANALYTICS);

                    return response;
                });
    }
}
