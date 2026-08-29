package net.akive.api.plan.adapter.webapi.weeklyplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.adapter.webapi.weeklyplan.dto.response.WeeklyPlanFindResponse;
import net.akive.api.plan.application.provided.weeklyplan.WeeklyPlanFinder;
import net.akive.api.plan.application.provided.weeklyplan.dto.WeeklyPlanFindQuery;
import net.akive.api.plan.application.result.WeeklyPlan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.WEEKLY_PLANS;

@RestController
@RequestMapping(WEEKLY_PLANS)
@RequiredArgsConstructor
public class WeeklyPlanApi {

    private final WeeklyPlanFinder weeklyPlanFinder;

    @GetMapping("/{weekNum}")
    public WeeklyPlanFindResponse find(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @PathVariable Integer weekNum
    ) {
        WeeklyPlan result = weeklyPlanFinder.find(new WeeklyPlanFindQuery(sessionalPlanId, userId, weekNum));

        return WeeklyPlanFindResponse.from(result);
    }
}
