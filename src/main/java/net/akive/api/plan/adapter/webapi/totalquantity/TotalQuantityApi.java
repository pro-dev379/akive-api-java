package net.akive.api.plan.adapter.webapi.totalquantity;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.response.SessionalPlanFindResponse;
import net.akive.api.plan.adapter.webapi.totalquantity.dto.request.TotalQuantityCreateRequest;
import net.akive.api.plan.adapter.webapi.totalquantity.dto.response.TotalQuantityCreateResponse;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityCreator;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityFinder;
import net.akive.api.plan.application.provided.totalquantity.dto.SessionalPlanTotalQuantityFindQuery;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityDeleteCommand;
import net.akive.api.plan.application.result.TotalQuantityProgress;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/sessional-plans/{sessionalPlanId}/total-quantities")
@RequiredArgsConstructor
public class TotalQuantityApi {

    private final TotalQuantityFinder totalQuantityFinder;
    private final TotalQuantityCreator totalQuantityCreator;

    @PostMapping
    @ResponseStatus(CREATED)
    public TotalQuantityCreateResponse create(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @Validated @RequestBody TotalQuantityCreateRequest request
    ) {
        return new TotalQuantityCreateResponse(totalQuantityCreator.create(request.to(userId, sessionalPlanId)));
    }

    @GetMapping("/{totalQuantityId}")
    public SessionalPlanFindResponse.TotalQuantityResponse find(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @PathVariable Long totalQuantityId
    ) {
        SessionalPlanTotalQuantityFindQuery query = new SessionalPlanTotalQuantityFindQuery(
                sessionalPlanId,
                userId,
                totalQuantityId
        );

        TotalQuantityProgress result = totalQuantityFinder.find(query);

        return SessionalPlanFindResponse.TotalQuantityResponse.from(result);
    }

    @DeleteMapping("/{totalQuantityId}")
    @ResponseStatus(NO_CONTENT)
    public void delete(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @PathVariable Long totalQuantityId
    ) {
        totalQuantityCreator.delete(new TotalQuantityDeleteCommand(sessionalPlanId, userId, totalQuantityId));
    }
}
