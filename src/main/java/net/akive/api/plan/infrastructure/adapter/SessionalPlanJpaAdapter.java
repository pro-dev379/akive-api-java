package net.akive.api.plan.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.port.out.*;
import net.akive.api.plan.application.port.out.dto.*;
import net.akive.api.plan.domain.*;
import net.akive.api.plan.infrastructure.repository.SessionalPlanJpaRepository;
import net.akive.api.plan.infrastructure.repository.SessionalPlanQueryRepository;
import net.akive.api.plan.infrastructure.entity.SessionPeriodEntity;
import net.akive.api.plan.infrastructure.entity.SessionalPlanEntity;
import net.akive.api.plan.infrastructure.projection.SessionalPlanTotalQuantityProjection;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
class SessionalPlanJpaAdapter implements
        LoadSessionalPlanPort,
        LoadTotalQuantityPort,
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
    public Optional<SessionPeriod> loadSessionPeriodBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId) {
        return sessionalPlanJpaRepository
                .findSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .map(SessionPeriodEntity::to);
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

        Map<WeekPeriod, SessionWeek> sessionWeekByPeriod = sessionWeeks.stream()
                .collect(Collectors.toMap(
                        SessionWeek::weekPeriod,
                        Function.identity()
                ));

        return projections.stream()
                .collect(Collectors.groupingBy(
                        SessionalPlanTotalQuantityProjection::totalQuantityId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .values()
                .stream()
                .map(group -> toSessionalPlanTotalQuantityData(group, sessionWeekByPeriod))
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

    private SessionalPlanTotalQuantityData toSessionalPlanTotalQuantityData(
            List<SessionalPlanTotalQuantityProjection> projections,
            Map<WeekPeriod, SessionWeek> sessionWeekByPeriod
    ) {
        SessionalPlanTotalQuantityProjection first = projections.getFirst();

        return new SessionalPlanTotalQuantityData(
                first.totalQuantityId(),
                first.sessionalPlanId(),
                first.subject().to(),
                first.totalQuantityAmount().to(),
                projections.stream()
                        .map(projection -> toSessionalPlanWeeklyQuantityData(projection, sessionWeekByPeriod))
                        .toList()
        );
    }

    private SessionalPlanWeeklyQuantityData toSessionalPlanWeeklyQuantityData(
            SessionalPlanTotalQuantityProjection projection,
            Map<WeekPeriod, SessionWeek> sessionWeekByPeriod
    ) {
        WeekPeriod weekPeriod = projection.weekPeriod().to();
        SessionWeek sessionWeek = Objects.requireNonNull(sessionWeekByPeriod.get(weekPeriod));
        SessionalPlanWeeklyQuantityAmountData weeklyQuantityAmount = new SessionalPlanWeeklyQuantityAmountData(
                projection.weeklyQuantityAmount().getQuantityAmount(),
                Math.toIntExact(projection.weeklyCompletedQuantityAmount()),
                Math.toIntExact(projection.weeklyDelayAmount()),
                projection.weeklyQuantityAmount().getQuantityMemo()
        );

        return new SessionalPlanWeeklyQuantityData(
                projection.weeklyQuantityId(),
                projection.totalQuantityId(),
                sessionWeek,
                weeklyQuantityAmount
        );
    }
}
