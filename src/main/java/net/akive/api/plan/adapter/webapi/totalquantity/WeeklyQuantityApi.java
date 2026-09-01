package net.akive.api.plan.adapter.webapi.totalquantity;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.adapter.webapi.totalquantity.dto.request.WeeklyQuantityDelayRequest;
import net.akive.api.plan.adapter.webapi.totalquantity.dto.request.WeeklyQuantityReduceRequest;
import net.akive.api.plan.adapter.webapi.weeklyplan.dto.response.WeeklyPlanFindResponse;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityModifier;
import net.akive.api.plan.application.provided.totalquantity.WeeklyQuantityFinder;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyPlanItemFindQuery;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyQuantityDelayCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyQuantityReduceCommand;
import net.akive.api.plan.application.result.WeeklyPlan;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.WEEKLY_QUANTITY;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping(WEEKLY_QUANTITY)
@RequiredArgsConstructor
public class WeeklyQuantityApi {

    private final WeeklyQuantityFinder weeklyQuantityFinder;
    private final TotalQuantityModifier totalQuantityModifier;

    @GetMapping
    public WeeklyPlanFindResponse.WeeklyQuantityResponse find(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @PathVariable Long totalQuantityId,
            @PathVariable Long weeklyQuantityId
    ) {
        WeeklyPlanItemFindQuery query = new WeeklyPlanItemFindQuery(
                sessionalPlanId,
                userId,
                totalQuantityId,
                weeklyQuantityId
        );

        WeeklyPlan.WeeklyPlanItem result = weeklyQuantityFinder.findWeeklyPlanItem(query);

        return WeeklyPlanFindResponse.WeeklyQuantityResponse.from(result);
    }

    @PatchMapping
    @ResponseStatus(NO_CONTENT)
    public void reduceTo(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @PathVariable Long totalQuantityId,
            @PathVariable Long weeklyQuantityId,
            @Validated @RequestBody WeeklyQuantityReduceRequest request
    ) {
        WeeklyQuantityReduceCommand command = request.to(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId
        );

        totalQuantityModifier.reduceWeeklyQuantityTo(command);
    }

    @PatchMapping("/delays")
    @ResponseStatus(NO_CONTENT)
    public void delayTo(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @PathVariable Long totalQuantityId,
            @PathVariable Long weeklyQuantityId,
            @Validated @RequestBody WeeklyQuantityDelayRequest request
    ) {
        WeeklyQuantityDelayCommand command = WeeklyQuantityDelayCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                request.targetWeeklyQuantityId(),
                request.quantityAmountToDelay()
        );

        totalQuantityModifier.delayWeeklyQuantityTo(command);
    }
}
