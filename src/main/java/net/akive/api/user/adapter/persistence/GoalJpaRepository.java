package net.akive.api.user.adapter.persistence;

import net.akive.api.user.adapter.persistence.entity.GoalEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface GoalJpaRepository extends JpaRepository<GoalEntity, Long> {

    @Override
    @NotNull
    @EntityGraph(attributePaths = "user")
    Optional<GoalEntity> findById(@NotNull Long goalId);

    @Query("""
        select g
        from GoalEntity g
        join fetch g.user u
        where u.userId = :userId
          and u.withdrawal.withdrawnAt is null
        order by g.goalId desc
        limit 1
    """)
    Optional<GoalEntity> findLatestByUserIdAndUserNotWithdrawn(Long userId);

    @Query("""
        select g
        from GoalEntity g
        join fetch g.user u
        where u.userId = :userId
          and g.createdAt < :createdAtBefore
        order by g.createdAt desc, g.goalId desc
        limit 1
    """)
    Optional<GoalEntity> findLatestByUserIdAndCreatedAtBefore(Long userId, Instant createdAtBefore);

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
            Instant startInclusive,
            Instant endExclusive
    );
}
