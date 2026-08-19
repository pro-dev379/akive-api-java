package net.akive.api.plan.apapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.apapter.persistence.entity.SessionPeriodEntity;
import net.akive.api.plan.apapter.persistence.entity.SessionalPlanEntity;
import net.akive.api.plan.apapter.persistence.projection.SessionalPlanTotalQuantityProjection;
import net.akive.api.plan.application.required.*;
import net.akive.api.plan.application.required.dto.SessionalPlanTotalQuantityData;
import net.akive.api.plan.application.required.dto.SessionalPlanWeeklyQuantityAmountData;
import net.akive.api.plan.application.required.dto.SessionalPlanWeeklyQuantityData;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionWeek;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.WeekPeriod;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
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
        List<SessionalPlanTotalQuantityProjection> projections = sessionalPlanJpaRepository
                .findSessionalPlanTotalQuantitiesBySessionalPlanId(sessionalPlanId);

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
    public Optional<SessionalPlanTotalQuantityData> loadSessionalPlanTotalQuantityBySessionalPlanIdAndTotalQuantityId(
            Long sessionalPlanId,
            Long totalQuantityId,
            List<SessionWeek> sessionWeeks
    ) {
        List<SessionalPlanTotalQuantityProjection> projections =
                sessionalPlanJpaRepository.findSessionalPlanTotalQuantitiesBySessionalPlanIdAndTotalQuantityId(
                        sessionalPlanId,
                        totalQuantityId
                );

        if (projections.isEmpty()) {
            return Optional.empty();
        }

        Map<WeekPeriod, SessionWeek> sessionWeekByPeriod = sessionWeeks.stream()
                .collect(Collectors.toMap(
                        SessionWeek::weekPeriod,
                        Function.identity()
                ));

        return Optional.of(toSessionalPlanTotalQuantityData(projections, sessionWeekByPeriod));
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
        return Math.toIntExact(sessionalPlanJpaRepository.countByUser_UserIdAndSessionPeriod_EndOnGreaterThanEqual(userId, now));
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
