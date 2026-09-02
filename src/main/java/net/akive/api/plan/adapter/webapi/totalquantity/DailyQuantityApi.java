package net.akive.api.plan.adapter.webapi.totalquantity;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.adapter.webapi.totalquantity.dto.request.DailyAmountUpdateRequest;
import net.akive.api.plan.adapter.webapi.totalquantity.dto.request.DailyCompletedAmountUpdateRequest;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityModifier;
import net.akive.api.plan.application.provided.totalquantity.dto.DailyAmountUpdateCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.DailyCompletedAmountUpdateCommand;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.DAILY_QUANTITIES;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping(DAILY_QUANTITIES)
@RequiredArgsConstructor
public class DailyQuantityApi {

    public final TotalQuantityModifier totalQuantityModifier;

    @PatchMapping("/{dailyQuantityId}")
    @ResponseStatus(NO_CONTENT)
    public void updateAmount(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @PathVariable Long totalQuantityId,
            @PathVariable Long weeklyQuantityId,
            @PathVariable Long dailyQuantityId,
            @Validated @RequestBody DailyAmountUpdateRequest request
    ) {
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                request.targetDailyAmount().quantityAmount(),
                request.targetDailyAmount().quantityMemo()
        );

        totalQuantityModifier.updateDailyAmount(command);
    }

    @PatchMapping("/{dailyQuantityId}/completed-quantities")
    @ResponseStatus(NO_CONTENT)
    public void updateCompletedAmount(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @PathVariable Long totalQuantityId,
            @PathVariable Long weeklyQuantityId,
            @PathVariable Long dailyQuantityId,
            @Validated @RequestBody DailyCompletedAmountUpdateRequest request
    ) {
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                request.targetDailyCompletedAmount()
        );

        totalQuantityModifier.updateDailyCompletedAmount(command);
    }
}
