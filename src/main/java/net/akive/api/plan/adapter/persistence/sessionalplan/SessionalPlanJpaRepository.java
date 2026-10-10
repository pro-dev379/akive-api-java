package net.akive.api.plan.adapter.persistence.sessionalplan;

import net.akive.api.plan.adapter.persistence.entity.SessionPeriodEntity;
import net.akive.api.plan.adapter.persistence.entity.SessionalPlanEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface SessionalPlanJpaRepository extends JpaRepository<SessionalPlanEntity, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<SessionalPlanEntity> findBySessionalPlanIdAndUser_UserId(Long sessionalPlanId, Long userId);

    @EntityGraph(attributePaths = "user")
    List<SessionalPlanEntity> findByUser_UserIdOrderBySessionalPlanIdDesc(Long userId);

    @Query("""
        select sp.sessionPeriod
        from SessionalPlanEntity sp
        where sp.sessionalPlanId = :sessionalPlanId
            and sp.user.userId = :userId
    """)
    Optional<SessionPeriodEntity> findSessionPeriodBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId);

    @Query("""
        select sp.sessionPeriod
        from SessionalPlanEntity sp
        where sp.user.userId = :userId
        order by sp.sessionPeriod.startOn asc, sp.sessionPeriod.endOn asc
    """)
    List<SessionPeriodEntity> findSessionPeriodsByUserId(Long userId);

    boolean existsBySessionalPlanIdAndUser_UserId(Long sessionalPlanId, Long userId);

    boolean existsByUser_UserIdAndSessionName(Long userId, String sessionName);

    boolean existsByUser_UserIdAndSessionNameAndSessionalPlanIdNot(
            Long userId,
            String sessionName,
            Long sessionalPlanId
    );

    long countByUser_UserIdAndSessionPeriod_EndOnGreaterThanEqual(Long userId, LocalDate now);
}
