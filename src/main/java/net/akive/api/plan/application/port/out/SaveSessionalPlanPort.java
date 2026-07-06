package net.akive.api.plan.application.port.out;

import net.akive.api.plan.domain.SessionalPlan;

public interface SaveSessionalPlanPort {

    SessionalPlan save(SessionalPlan sessionalPlan);
}
