package net.akive.api.plan.application.port.out;

import net.akive.api.plan.domain.SessionalPlan;

import java.util.List;

public interface LoadSessionalPlanPort {

    List<SessionalPlan> loadAllByUserIdOrderBySessionalPlanIdDesc(Long userId);
}