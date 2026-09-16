package net.akive.api.plan.adapter.persistence.sessionalplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.adapter.persistence.entity.SessionPeriodEntity;
import net.akive.api.plan.adapter.persistence.entity.SessionalPlanEntity;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.value.SessionPeriod;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Repository
@RequiredArgsConstructor
class SessionalPlanJpaAdapter implements SessionalPlanRepository {

    private final SessionalPlanJpaRepository sessionalPlanJpaRepository;

    @Override
    public SessionalPlan save(SessionalPlan sessionalPlan) {
        return sessionalPlanJpaRepository.save(SessionalPlanEntity.from(sessionalPlan)).to();
    }

    @Override
    public Optional<SessionalPlan> findBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId) {
        return sessionalPlanJpaRepository
                .findBySessionalPlanIdAndUser_UserId(sessionalPlanId, userId)
                .map(SessionalPlanEntity::to);
    }

    @Override
    public List<SessionalPlan> findAllByUserIdOrderBySessionalPlanIdDesc(Long userId) {
        return sessionalPlanJpaRepository
                .findByUser_UserId(userId, Sort.by(DESC, "sessionalPlanId"))
                .stream()
                .map(SessionalPlanEntity::to)
                .toList();
    }

    @Override
    public Optional<SessionPeriod> findSessionPeriodBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId) {
        return sessionalPlanJpaRepository
                .findSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .map(SessionPeriodEntity::to);
    }

    @Override
    public List<SessionPeriod> findSessionPeriodsByUserId(Long userId) {
        return sessionalPlanJpaRepository.findSessionPeriodsByUserId(userId)
                .stream()
                .map(SessionPeriodEntity::to)
                .toList();
    }

    @Override
    public boolean existsBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId) {
        return sessionalPlanJpaRepository.existsBySessionalPlanIdAndUser_UserId(sessionalPlanId, userId);
    }

    @Override
    public boolean existsByUserIdAndSessionName(Long userId, String sessionName) {
        return sessionalPlanJpaRepository.existsByUser_UserIdAndSessionName(userId, sessionName);
    }

    @Override
    public boolean existsByUserIdAndSessionNameAndSessionalPlanIdNot(
            Long userId,
            String sessionName,
            Long sessionalPlanId
    ) {
        return sessionalPlanJpaRepository.existsByUser_UserIdAndSessionNameAndSessionalPlanIdNot(
                userId,
                sessionName,
                sessionalPlanId
        );
    }

    @Override
    public void delete(SessionalPlan sessionalPlan) {
        sessionalPlanJpaRepository.delete(SessionalPlanEntity.from(sessionalPlan));
    }

    @Override
    public long countInProgressByUserId(Long userId, LocalDate now) {
        return sessionalPlanJpaRepository.countByUser_UserIdAndSessionPeriod_EndOnGreaterThanEqual(userId, now);
    }
}
