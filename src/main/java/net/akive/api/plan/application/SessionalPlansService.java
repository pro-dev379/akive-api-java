package net.akive.api.plan.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.dto.result.SessionalPlanResult;
import net.akive.api.plan.application.port.in.GetSessionalPlansUseCase;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionalPlansService implements GetSessionalPlansUseCase {

    private final LoadSessionalPlanPort loadSessionalPlanPort;

    @Override
    public List<SessionalPlanResult> getSessionalPlans(Long userId) {
        return loadSessionalPlanPort
                .loadAllByUserIdOrderBySessionalPlanIdDesc(userId)
                .stream()
                .map(SessionalPlanResult::from)
                .toList();
    }
}