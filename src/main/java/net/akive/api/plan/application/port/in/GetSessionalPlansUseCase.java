package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;

import java.util.List;

public interface GetSessionalPlansUseCase {

    List<SessionalPlanSummaryResult> getAll(Long userId);
}