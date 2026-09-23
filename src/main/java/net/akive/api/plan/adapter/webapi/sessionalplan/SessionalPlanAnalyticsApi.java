package net.akive.api.plan.adapter.webapi.sessionalplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.response.CompletionRateFindResponse;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.plan.application.provided.sessionalplan.dto.CompletionRateFindQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.SESSIONAL_PLAN_ANALYTICS;

@RestController
@RequestMapping(SESSIONAL_PLAN_ANALYTICS)
@RequiredArgsConstructor
public class SessionalPlanAnalyticsApi {

    private final SessionalPlanFinder sessionalPlanFinder;

    @GetMapping("/completion-rates/metrics")
    public CompletionRateFindResponse findCompletionRate(
            @AuthenticatedUserId Long userId,
            @RequestParam LocalDate startOn,
            @RequestParam LocalDate endOn
    ) {
        CompletionRateFindQuery query = new CompletionRateFindQuery(userId, startOn, endOn);

        return CompletionRateFindResponse.from(sessionalPlanFinder.findCompletionRate(query));
    }
}
