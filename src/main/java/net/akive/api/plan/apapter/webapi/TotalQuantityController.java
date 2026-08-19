package net.akive.api.plan.apapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.apapter.webapi.dto.request.CreateTotalQuantityRequest;
import net.akive.api.plan.apapter.webapi.dto.response.CreateTotalQuantityResponse;
import net.akive.api.plan.application.provided.CreateTotalQuantityUseCase;
import net.akive.api.plan.application.provided.DeleteTotalQuantityUseCase;
import net.akive.api.plan.application.provided.GetSessionalPlanTotalQuantityUseCase;
import net.akive.api.plan.application.provided.dto.command.DeleteTotalQuantityCommand;
import net.akive.api.plan.application.provided.dto.query.GetSessionalPlanTotalQuantityQuery;
import net.akive.api.plan.application.provided.dto.result.sessionalplan.GetSessionalPlanResult;
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
