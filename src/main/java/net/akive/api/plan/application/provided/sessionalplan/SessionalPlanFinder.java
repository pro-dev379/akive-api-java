package net.akive.api.plan.application.provided.sessionalplan;

import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanFindQuery;
import net.akive.api.plan.application.result.SessionalPlanDetail;
import net.akive.api.plan.domain.model.SessionalPlan;

import java.util.List;

public interface SessionalPlanFinder {

    List<SessionalPlan> findAll(Long userId);

    SessionalPlanDetail find(SessionalPlanFindQuery query) throws NotFoundException;
}