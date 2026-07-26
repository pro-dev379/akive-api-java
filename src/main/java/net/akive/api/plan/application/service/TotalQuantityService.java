package net.akive.api.plan.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.dto.command.CreateTotalQuantityCommand;
import net.akive.api.plan.application.dto.command.DeleteTotalQuantityCommand;
import net.akive.api.plan.application.dto.query.GetSessionalPlanTotalQuantityQuery;
import net.akive.api.plan.application.dto.result.sessionalplan.GetSessionalPlanResult;
import net.akive.api.plan.application.port.in.CreateTotalQuantityUseCase;
import net.akive.api.plan.application.port.in.DeleteTotalQuantityUseCase;
import net.akive.api.plan.application.port.in.GetSessionalPlanTotalQuantityUseCase;
import net.akive.api.plan.application.port.out.DeleteTotalQuantityPort;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.application.port.out.LoadTotalQuantityPort;
import net.akive.api.plan.application.port.out.SaveTotalQuantityPort;
import net.akive.api.plan.application.port.out.dto.SessionalPlanTotalQuantityData;
import net.akive.api.plan.domain.SessionWeek;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.TotalQuantityPlan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_TOTAL_QUANTITY_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
class TotalQuantityService implements
        GetSessionalPlanTotalQuantityUseCase,
        CreateTotalQuantityUseCase,
        DeleteTotalQuantityUseCase {

    private final LoadSessionalPlanPort loadSessionalPlanPort;
    private final LoadTotalQuantityPort loadTotalQuantityPort;
    private final SaveTotalQuantityPort saveTotalQuantityPort;
    private final DeleteTotalQuantityPort deleteTotalQuantityPort;

    private final TotalQuantityDomainService totalQuantityDomainService;

    @Override
    @Transactional(readOnly = true)
    public GetSessionalPlanResult.TotalQuantityResult get(GetSessionalPlanTotalQuantityQuery query) {
        SessionalPlan sessionalPlan = loadSessionalPlanOrThrow(query.sessionalPlanId(), query.userId());
        List<SessionWeek> sessionWeeks = sessionalPlan.getSessionPeriod().splitIntoWeeks();
        SessionalPlanTotalQuantityData data = loadSessionalPlanTotalQuantityOrThrow(query.sessionalPlanId(), query.totalQuantityId(), sessionWeeks);

        return GetSessionalPlanResult.TotalQuantityResult.from(data);
    }

    @Override
    public Long create(CreateTotalQuantityCommand command) {
        TotalQuantityPlan totalQuantityPlan = totalQuantityDomainService.create(
                command.sessionalPlanId(),
                command.userId(),
                command.subject(),
                command.totalQuantityAmount()
        );

        return saveTotalQuantityPort.save(totalQuantityPlan);
    }

    @Override
    public void delete(DeleteTotalQuantityCommand command) {
        long deletedCount = deleteTotalQuantityPort.deleteByTotalQuantityIdAndSessionalPlanIdAndUserId(
                command.totalQuantityId(),
                command.sessionalPlanId(),
                command.userId()
        );

        if (deletedCount == 0) {
            throw new NotFoundException(SESSIONAL_PLAN_TOTAL_QUANTITY_NOT_FOUND);
        }
    }

    private SessionalPlan loadSessionalPlanOrThrow(Long sessionalPlanId, Long userId) {
        return loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new NotFoundException(SESSIONAL_PLAN_NOT_FOUND));
    }

    private SessionalPlanTotalQuantityData loadSessionalPlanTotalQuantityOrThrow(
            Long sessionalPlanId,
            Long totalQuantityId,
            List<SessionWeek> sessionWeeks
    ) {
        return loadTotalQuantityPort
                .loadSessionalPlanTotalQuantityBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId, sessionWeeks)
                .orElseThrow(() -> new NotFoundException(SESSIONAL_PLAN_TOTAL_QUANTITY_NOT_FOUND));
    }
}
