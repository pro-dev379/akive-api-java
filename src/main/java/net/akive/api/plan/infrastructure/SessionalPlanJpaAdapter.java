package net.akive.api.plan.infrastructure;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.port.out.*;
import net.akive.api.plan.application.port.out.dto.DailyTodoData;
import net.akive.api.plan.application.port.out.dto.SessionalPlanTotalQuantityData;
import net.akive.api.plan.application.port.out.dto.SessionalPlanWeeklyQuantityAmountData;
import net.akive.api.plan.application.port.out.dto.SessionalPlanWeeklyQuantityData;
import net.akive.api.plan.domain.DailyQuantityAmount;
import net.akive.api.plan.domain.SessionWeek;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.Subject;
import net.akive.api.plan.domain.WeekPeriod;
import net.akive.api.plan.infrastructure.entity.SessionalPlanEntity;
import net.akive.api.plan.infrastructure.projection.DailyTodoProjection;
import net.akive.api.plan.infrastructure.projection.SessionalPlanTotalQuantityProjection;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
class SessionalPlanJpaAdapter implements
        LoadSessionalPlanPort,
        LoadTotalQuantitiesPort,
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
    public List<SessionalPlanTotalQuantityData> loadSessionalPlanTotalQuantitiesBySessionalPlanId(
            Long sessionalPlanId,
            List<SessionWeek> sessionWeeks
    ) {
        List<SessionalPlanTotalQuantityProjection> projections = sessionalPlanQueryRepository
                .findSessionalPlanTotalQuantityDetailsBySessionalPlanId(sessionalPlanId);

        Map<WeekPeriod, Integer> weekNumByPeriod = sessionWeeks.stream()
                .collect(Collectors.toMap(
                        SessionWeek::weekPeriod,
                        SessionWeek::weekNum
                ));

        return projections.stream()
                .collect(Collectors.groupingBy(
                        SessionalPlanTotalQuantityProjection::totalQuantityId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .values()
                .stream()
                .map(group -> toSessionalPlanTotalQuantityData(group, weekNumByPeriod))
                .toList();
    }

    @Override
    public List<DailyTodoData> loadDailyTodosByUserIdAndDate(Long userId, LocalDate date) {
        return sessionalPlanQueryRepository
                .findDailyTodosByUserIdAndDate(userId, date)
                .stream()
                .map(this::toDailyTodoData)
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

    private DailyTodoData toDailyTodoData(DailyTodoProjection projection) {
        return new DailyTodoData(
                projection.sessionalPlanId(),
                projection.totalQuantityId(),
                projection.weeklyQuantityId(),
                projection.dailyQuantityId(),
                new Subject(
                        projection.subjectName(),
                        projection.subjectCategory()
                ),
                DailyQuantityAmount.of(
                        projection.dailyQuantityAmount(),
                        projection.completedQuantityAmount(),
                        projection.delayedQuantityAmount(),
                        projection.dailyQuantityMemo()
                ),
                projection.unit(),
                projection.date()
        );
    }

    private SessionalPlanTotalQuantityData toSessionalPlanTotalQuantityData(
            List<SessionalPlanTotalQuantityProjection> projections,
            Map<WeekPeriod, Integer> weekNumByPeriod
    ) {
        SessionalPlanTotalQuantityProjection first = projections.getFirst();

        return new SessionalPlanTotalQuantityData(
                first.totalQuantityId(),
                first.sessionalPlanId(),
                first.subject(),
                first.subjectCategory(),
                first.totalQuantityAmount(),
                first.unit(),
                first.totalQuantityMemo(),
                projections.stream()
                        .map(projection -> toSessionalPlanWeeklyQuantityData(projection, weekNumByPeriod))
                        .toList()
        );
    }

    private SessionalPlanWeeklyQuantityData toSessionalPlanWeeklyQuantityData(
            SessionalPlanTotalQuantityProjection projection,
            Map<WeekPeriod, Integer> weekNumByPeriod
    ) {
        WeekPeriod weekPeriod = new WeekPeriod(projection.weekStartOn(), projection.weekEndOn());
        SessionalPlanWeeklyQuantityAmountData weeklyQuantityAmount = new SessionalPlanWeeklyQuantityAmountData(
                projection.weeklyQuantityAmount(),
                Math.toIntExact(projection.weeklyCompletedQuantityAmount()),
                Math.toIntExact(projection.weeklyDelayAmount()),
                projection.weeklyQuantityMemo()
        );

        return new SessionalPlanWeeklyQuantityData(
                projection.weeklyQuantityId(),
                projection.totalQuantityId(),
                weekNumByPeriod.get(weekPeriod),
                weekPeriod.startOn(),
                weekPeriod.endOn(),
                weeklyQuantityAmount
        );
    }
}
