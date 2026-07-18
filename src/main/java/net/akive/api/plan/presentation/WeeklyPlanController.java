package net.akive.api.plan.presentation;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.application.dto.command.GetWeeklyPlanCommand;
import net.akive.api.plan.application.dto.result.weeklyplan.GetWeeklyPlanResult;
import net.akive.api.plan.application.port.in.GetWeeklyPlanUseCase;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sessional-plans/{sessionalPlanId}/weekly-plans")
@RequiredArgsConstructor
public class WeeklyPlanController {

    private final GetWeeklyPlanUseCase getWeeklyPlanUseCase;

    @GetMapping("/{weekNum}")
    public GetWeeklyPlanResult get(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @PathVariable Integer weekNum
    ) {
        return getWeeklyPlanUseCase.get(new GetWeeklyPlanCommand(sessionalPlanId, userId, weekNum));
    }
}