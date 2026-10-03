package net.akive.api.plan.application.provided.sessionalplan;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.sessionalplan.dto.CompletionRateFindQuery;
import net.akive.api.plan.application.provided.sessionalplan.dto.CompletionRateSeriesFindQuery;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanDetailFindQuery;
import net.akive.api.plan.application.result.SessionalPlanDetail;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.value.CompletionRate;
import net.akive.api.plan.domain.value.CompletionRateSeries;
import net.akive.api.plan.domain.value.SessionPeriod;

import java.util.List;

public interface SessionalPlanFinder {

    List<SessionalPlan> findAll(Long userId);

    SessionalPlanDetail findDetail(SessionalPlanDetailFindQuery query) throws ResourceNotFoundException;

    List<SessionPeriod> findSessionPeriods(Long userId);

    CompletionRate findCompletionRate(CompletionRateFindQuery query);

    CompletionRateSeries findCompletionRateSeries(CompletionRateSeriesFindQuery query);
}
