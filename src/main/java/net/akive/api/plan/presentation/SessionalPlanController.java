package net.akive.api.plan.presentation;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.dto.result.SessionalPlanResult;
import net.akive.api.plan.application.port.in.GetSessionalPlansUseCase;
import net.akive.api.plan.presentation.dto.response.SessionalPlansResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sessional-plans")
@RequiredArgsConstructor
public class SessionalPlanController {

    private final GetSessionalPlansUseCase getSessionalPlansUseCase;

    @GetMapping
    public SessionalPlansResponse getSessionalPlans(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        List<SessionalPlanResult> results = getSessionalPlansUseCase.getSessionalPlans(userId);

        return SessionalPlansResponse.from(results);
    }
}