package net.akive.api.plan.presentation;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.application.port.in.CreateTotalQuantityUseCase;
import net.akive.api.plan.presentation.dto.request.CreateTotalQuantityRequest;
import net.akive.api.plan.presentation.dto.response.CreateTotalQuantityResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/sessional-plans/{sessionalPlanId}/total-quantities")
@RequiredArgsConstructor
public class TotalQuantityController {

    private final CreateTotalQuantityUseCase createTotalQuantityUseCase;

    @PostMapping
    @ResponseStatus(CREATED)
    public CreateTotalQuantityResponse create(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @Validated @RequestBody CreateTotalQuantityRequest request
    ) {
        return new CreateTotalQuantityResponse(createTotalQuantityUseCase.create(request.to(userId, sessionalPlanId)));
    }
}