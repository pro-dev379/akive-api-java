package net.akive.api.user.adapter.persistence;

import net.akive.api.user.adapter.persistence.entity.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GoalJpaRepository extends JpaRepository<GoalEntity, Long> {

    @Query("""
        select g
        from GoalEntity g
        where g.user.userId = :userId
          and g.user.withdrawal.withdrawnAt is null
        order by g.goalId desc
        limit 1
    """)
    Optional<GoalEntity> findLatestByUserIdAndUserNotWithdrawn(Long userId);

    @Query("""
        select g
        from GoalEntity g
        where g.user.userId = :userId
          and g.createdAt < :createdAtBefore
        order by g.createdAt desc, g.goalId desc
        limit 1
    """)
    Optional<GoalEntity> findLatestByUserIdAndCreatedAtBefore(Long userId, LocalDateTime createdAtBefore);

    @Query("""
        select g
        from GoalEntity g
        where g.user.userId = :userId
          and g.createdAt >= :startInclusive
          and g.createdAt < :endExclusive
        order by g.createdAt asc, g.goalId asc
    """)
    List<GoalEntity> findAllByUserIdAndCreatedAtInRange(
            Long userId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );
}
