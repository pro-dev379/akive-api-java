package net.akive.api.plan.application.required;

import net.akive.api.plan.domain.SessionalPlan;

public interface SaveSessionalPlanPort {

    SessionalPlan save(SessionalPlan sessionalPlan);
}
