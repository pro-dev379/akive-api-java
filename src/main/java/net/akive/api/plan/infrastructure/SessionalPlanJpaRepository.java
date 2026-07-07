package net.akive.api.plan.infrastructure;

import net.akive.api.plan.infrastructure.entity.SessionalPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SessionalPlanJpaRepository extends JpaRepository<SessionalPlanEntity, Long> {

    List<SessionalPlanEntity> findByUser_userIdOrderBySessionalPlanIdDesc(Long userId);
    boolean existsByUser_userIdAndSessionName(Long userId, String sessionName);

    @Modifying(clearAutomatically = true)
    @Query("""
            delete from SessionalPlanEntity sp
            where sp.sessionalPlanId = :sessionalPlanId
                and sp.user.userId = :userId
           """)
    long deleteBySessionalPlanIdAndUserId(Long sessionalPlanId, Long userId);
}