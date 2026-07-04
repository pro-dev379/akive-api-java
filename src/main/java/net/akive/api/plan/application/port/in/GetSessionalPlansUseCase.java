package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.result.SessionalPlanResult;

import java.util.List;

public interface GetSessionalPlansUseCase {

    List<SessionalPlanResult> getSessionalPlans(Long userId);
}