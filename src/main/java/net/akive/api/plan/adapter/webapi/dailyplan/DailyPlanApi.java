package net.akive.api.plan.adapter.webapi.dailyplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.adapter.webapi.dailyplan.dto.response.DailyPlansFindResponse;
import net.akive.api.plan.application.provided.dailyplan.DailyPlanFinder;
import net.akive.api.plan.application.provided.dailyplan.dto.DailyPlansFindQuery;
import net.akive.api.plan.application.result.DailyPlan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.DAILY_PLANS;

@RestController
@RequestMapping(DAILY_PLANS)
@RequiredArgsConstructor
public class DailyPlanApi {

    private final DailyPlanFinder dailyPlanFinder;

    @GetMapping("/{date}")
    public DailyPlansFindResponse find(@AuthenticatedUserId Long userId, @PathVariable LocalDate date) {
        List<DailyPlan> result = dailyPlanFinder.find(new DailyPlansFindQuery(userId, date));

        return DailyPlansFindResponse.from(result);
    }
}
