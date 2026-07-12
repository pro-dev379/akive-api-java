package net.akive.api.plan.infrastructure;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.dto.result.DailyTodoResult;
import net.akive.api.plan.application.port.out.*;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.infrastructure.entity.SessionalPlanEntity;
import net.akive.api.plan.infrastructure.projection.DailyTodoProjection;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class SessionalPlanJpaAdapter implements
        LoadSessionalPlanPort,
        LoadDailyTodosPort,
        SaveSessionalPlanPort,
        DeleteSessionalPlanPort,
        ValidateSessionalPlanPort {

    private final SessionalPlanJpaRepository sessionalPlanJpaRepository;
    private final SessionalPlanQueryRepository sessionalPlanQueryRepository;

    @Override
    public Optional<SessionalPlan> loadBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId) {
        return sessionalPlanJpaRepository
                .findBySessionalPlanIdAndUser_UserId(sessionalPlanId, userId)
                .map(SessionalPlanEntity::to);
    }

    @Override
    public List<SessionalPlan> loadAllByUserIdOrderBySessionalPlanIdDesc(Long userId) {
        return sessionalPlanJpaRepository
                .findByUser_UserIdOrderBySessionalPlanIdDesc(userId)
                .stream()
                .map(SessionalPlanEntity::to)
                .toList();
    }

    @Override
    public List<DailyTodoResult> loadDailyTodosByUserIdAndDate(Long userId, LocalDate date) {
        return sessionalPlanQueryRepository
                .findDailyTodosByUserIdAndDate(userId, date)
                .stream()
                .map(this::toDailyTodoResult)
                .toList();
    }

    @Override
    public SessionalPlan save(SessionalPlan sessionalPlan) {
        return sessionalPlanJpaRepository.save(SessionalPlanEntity.from(sessionalPlan)).to();
    }

    @Override
    public long deleteBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId) {
        return sessionalPlanJpaRepository.deleteBySessionalPlanIdAndUserId(sessionalPlanId, userId);
    }

    @Override
    public boolean existsByUserIdAndSessionName(Long userId, String sessionName) {
        return sessionalPlanJpaRepository.existsByUser_UserIdAndSessionName(userId, sessionName);
    }

    @Override
    public boolean existsByUserIdAndSessionNameAndSessionalPlanIdNot(Long userId, String sessionName, Long sessionalPlanId) {
        return sessionalPlanJpaRepository
                .existsByUser_UserIdAndSessionNameAndSessionalPlanIdNot(userId, sessionName, sessionalPlanId);
    }

    @Override
    public int countInProgressByUserId(Long userId, LocalDate now) {
        return Math.toIntExact(
                sessionalPlanJpaRepository.countByUser_UserIdAndSessionPeriod_EndOnGreaterThanEqual(userId, now)
        );
    }

    private DailyTodoResult toDailyTodoResult(DailyTodoProjection projection) {
        return new DailyTodoResult(
                projection.sessionalPlanId(),
                projection.totalQuantityId(),
                projection.weeklyQuantityId(),
                projection.dailyQuantityId(),
                projection.subject(),
                projection.subjectCategory(),
                projection.dailyQuantityAmount(),
                projection.completedQuantityAmount(),
                projection.delayQuantityAmount(),
                projection.unit(),
                projection.dailyQuantityMemo(),
                projection.date()
        );
    }
}
