package net.akive.api.plan.adapter.webapi.sessionalplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.cache.ResponseCache;
import net.akive.api.common.analytics.TimeSeriesUnit;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.response.CompletionRateFindResponse;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.response.CompletionRateSeriesFindResponse;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.plan.application.provided.sessionalplan.dto.CompletionRateFindQuery;
import net.akive.api.plan.application.provided.sessionalplan.dto.CompletionRateSeriesFindQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.SESSIONAL_PLAN_ANALYTICS;
import static net.akive.api.common.cache.CacheKeyProvider.sessionalPlanCompletionRate;
import static net.akive.api.common.cache.CacheKeyProvider.sessionalPlanCompletionRateSeries;
import static net.akive.api.common.cache.ResponseCacheTtl.ANALYTICS;

@RestController
@RequestMapping(SESSIONAL_PLAN_ANALYTICS)
@RequiredArgsConstructor
public class SessionalPlanAnalyticsApi {

    private final SessionalPlanFinder sessionalPlanFinder;
    private final ResponseCache responseCache;

    @GetMapping("/completion-rates/metrics")
    public CompletionRateFindResponse findCompletionRate(
            @AuthenticatedUserId Long userId,
            @RequestParam LocalDate startOn,
            @RequestParam LocalDate endOn
    ) {
        CompletionRateFindQuery query = new CompletionRateFindQuery(userId, startOn, endOn);
        String cacheKey = sessionalPlanCompletionRate(userId, startOn, endOn);

        return responseCache.get(cacheKey, CompletionRateFindResponse.class)
                .orElseGet(() -> {
                    CompletionRateFindResponse response =
                            CompletionRateFindResponse.from(sessionalPlanFinder.findCompletionRate(query));
                    responseCache.put(cacheKey, response, ANALYTICS);

                    return response;
                });
    }

    @GetMapping("/completion-rates/series")
    public CompletionRateSeriesFindResponse findCompletionRateSeries(
            @AuthenticatedUserId Long userId,
            @RequestParam LocalDate startOn,
            @RequestParam LocalDate endOn,
            @RequestParam TimeSeriesUnit unit
    ) {
        CompletionRateSeriesFindQuery query = new CompletionRateSeriesFindQuery(userId, startOn, endOn, unit);
        String cacheKey = sessionalPlanCompletionRateSeries(userId, startOn, endOn, unit);

        return responseCache.get(cacheKey, CompletionRateSeriesFindResponse.class)
                .orElseGet(() -> {
                    CompletionRateSeriesFindResponse response = CompletionRateSeriesFindResponse.from(
                            sessionalPlanFinder.findCompletionRateSeries(query)
                    );
                    responseCache.put(cacheKey, response, ANALYTICS);

                    return response;
                });
    }
}
