package net.akive.api.dailylog.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.dailylog.adapter.webapi.dto.response.TimeUsageRatesFindResponse;
import net.akive.api.dailylog.application.provided.DailyLogFinder;
import net.akive.api.dailylog.application.provided.dto.TimeUsageRatesFindQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/daily-logs/analytics")
@RequiredArgsConstructor
public class DailyLogAnalyticsApi {

    private final DailyLogFinder dailyLogFinder;

    @GetMapping("/analytics/time-usage-rates")
    public TimeUsageRatesFindResponse findTimeUsageRates(
            @AuthenticatedUserId Long userId,
            @RequestParam LocalDate startOn,
            @RequestParam LocalDate endOn
    ) {
        TimeUsageRatesFindQuery query = new TimeUsageRatesFindQuery(userId, startOn, endOn);

        return TimeUsageRatesFindResponse.from(dailyLogFinder.findTimeUsageRates(query));
    }
}
