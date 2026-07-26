package net.akive.api.plan.presentation;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.application.dto.command.DeleteTotalQuantityCommand;
import net.akive.api.plan.application.dto.query.GetSessionalPlanTotalQuantityQuery;
import net.akive.api.plan.application.dto.result.sessionalplan.GetSessionalPlanResult;
import net.akive.api.plan.application.port.in.CreateTotalQuantityUseCase;
import net.akive.api.plan.application.port.in.DeleteTotalQuantityUseCase;
import net.akive.api.plan.application.port.in.GetSessionalPlanTotalQuantityUseCase;
import net.akive.api.plan.presentation.dto.request.CreateTotalQuantityRequest;
import net.akive.api.plan.presentation.dto.response.CreateTotalQuantityResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/sessional-plans/{sessionalPlanId}/total-quantities")
@RequiredArgsConstructor
public class TotalQuantityController {

    private final GetSessionalPlanTotalQuantityUseCase getSessionalPlanTotalQuantityUseCase;
    private final CreateTotalQuantityUseCase createTotalQuantityUseCase;
    private final DeleteTotalQuantityUseCase deleteTotalQuantityUseCase;

    @GetMapping("/{totalQuantityId}")
    public GetSessionalPlanResult.TotalQuantityResult get(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @PathVariable Long totalQuantityId
    ) {
        return getSessionalPlanTotalQuantityUseCase.get(
                new GetSessionalPlanTotalQuantityQuery(sessionalPlanId, userId, totalQuantityId)
        );
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public CreateTotalQuantityResponse create(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @Validated @RequestBody CreateTotalQuantityRequest request
    ) {
        return new CreateTotalQuantityResponse(createTotalQuantityUseCase.create(request.to(userId, sessionalPlanId)));
    }

    @DeleteMapping("/{totalQuantityId}")
    @ResponseStatus(NO_CONTENT)
    public void delete(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @PathVariable Long totalQuantityId
    ) {
        deleteTotalQuantityUseCase.delete(new DeleteTotalQuantityCommand(sessionalPlanId, userId, totalQuantityId));
    }
}
