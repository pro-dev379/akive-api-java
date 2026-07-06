package net.akive.api.plan.presentation;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;
import net.akive.api.plan.application.port.in.CreateSessionalPlanUseCase;
import net.akive.api.plan.application.port.in.GetSessionalPlansUseCase;
import net.akive.api.plan.presentation.dto.request.SessionalPlanCreateRequest;
import net.akive.api.plan.presentation.dto.response.SessionalPlanCreateResponse;
import net.akive.api.plan.presentation.dto.response.SessionalPlansResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/sessional-plans")
@RequiredArgsConstructor
public class SessionalPlanController {

    private final GetSessionalPlansUseCase getSessionalPlansUseCase;
    private final CreateSessionalPlanUseCase createSessionalPlanUseCase;

    @GetMapping
    public SessionalPlansResponse getAll(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        List<SessionalPlanSummaryResult> results = getSessionalPlansUseCase.getAll(userId);

        return SessionalPlansResponse.from(results);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public SessionalPlanCreateResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Validated @RequestBody SessionalPlanCreateRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        SessionalPlanSummaryResult result = createSessionalPlanUseCase.create(request.toCommand(userId));

        return SessionalPlanCreateResponse.from(result);
    }
}