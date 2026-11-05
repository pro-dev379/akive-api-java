package net.akive.api.plan.adapter.webapi.sessionalplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.cache.ResponseCache;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.request.SessionalPlanCreateRequest;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.request.SessionalPlanUpdateRequest;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.response.SessionalPlanCreateResponse;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.response.SessionalPlanDetailFindResponse;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.response.SessionalPlansFindResponse;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanCreator;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanDeleteCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanDetailFindQuery;
import net.akive.api.plan.application.result.SessionalPlanDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.SESSIONAL_PLANS;
import static net.akive.api.common.cache.CacheKeyProvider.sessionalPlanAnalyticsPattern;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping(SESSIONAL_PLANS)
@RequiredArgsConstructor
public class SessionalPlanApi {

    private final SessionalPlanFinder sessionalPlanFinder;
    private final SessionalPlanCreator sessionalPlanCreator;
    private final ResponseCache responseCache;

    @GetMapping
    public SessionalPlansFindResponse findAll(@AuthenticatedUserId Long userId) {
        return SessionalPlansFindResponse.from(sessionalPlanFinder.findAll(userId));
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public SessionalPlanCreateResponse create(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody SessionalPlanCreateRequest request
    ) {
        return new SessionalPlanCreateResponse(sessionalPlanCreator.create(request.to(userId)));
    }

    @GetMapping("/{sessionalPlanId}")
    public SessionalPlanDetailFindResponse findDetail(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId
    ) {
        SessionalPlanDetailFindQuery query = new SessionalPlanDetailFindQuery(userId, sessionalPlanId);

        SessionalPlanDetail result = sessionalPlanFinder.findDetail(query);

        return SessionalPlanDetailFindResponse.from(result);
    }

    @PutMapping("/{sessionalPlanId}")
    @ResponseStatus(NO_CONTENT)
    public void update(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @Validated @RequestBody SessionalPlanUpdateRequest request
    ) {
        sessionalPlanCreator.update(request.to(sessionalPlanId, userId));
        responseCache.evictByPattern(sessionalPlanAnalyticsPattern(userId));
    }

    @DeleteMapping("/{sessionalPlanId}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@AuthenticatedUserId Long userId, @PathVariable Long sessionalPlanId) {
        sessionalPlanCreator.delete(new SessionalPlanDeleteCommand(userId, sessionalPlanId));
        responseCache.evictByPattern(sessionalPlanAnalyticsPattern(userId));
    }
}
