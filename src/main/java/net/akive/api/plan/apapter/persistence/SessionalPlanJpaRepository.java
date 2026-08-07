package net.akive.api.plan.apapter.persistence;

import net.akive.api.plan.apapter.persistence.entity.SessionPeriodEntity;
import net.akive.api.plan.apapter.persistence.entity.SessionalPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SessionalPlanJpaRepository extends JpaRepository<SessionalPlanEntity, Long>, SessionalPlanQueryRepository {

    Optional<SessionalPlanEntity> findBySessionalPlanIdAndUser_UserId(Long sessionalPlanId, Long userId);

    @Query("""
            select sp.sessionPeriod
            from SessionalPlanEntity sp
            where sp.sessionalPlanId = :sessionalPlanId
                and sp.user.userId = :userId
            """)
    Optional<SessionPeriodEntity> findSessionPeriodBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId);

    List<SessionalPlanEntity> findByUser_UserIdOrderBySessionalPlanIdDesc(Long userId);
    boolean existsByUser_UserIdAndSessionName(Long userId, String sessionName);
    boolean existsByUser_UserIdAndSessionNameAndSessionalPlanIdNot(Long userId, String sessionName, Long sessionalPlanId);
    long countByUser_UserIdAndSessionPeriod_EndOnGreaterThanEqual(Long userId, LocalDate now);

    @Modifying(clearAutomatically = true)
    @Query("""
            delete from SessionalPlanEntity sp
            where sp.sessionalPlanId = :sessionalPlanId
                and sp.user.userId = :userId
           """)
    long deleteBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId);
}
