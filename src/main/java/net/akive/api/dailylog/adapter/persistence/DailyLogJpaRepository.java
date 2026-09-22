package net.akive.api.dailylog.adapter.persistence;

import net.akive.api.dailylog.adapter.persistence.entity.DailyLogEntity;
import net.akive.api.user.adapter.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DailyLogJpaRepository extends JpaRepository<DailyLogEntity, Long> {

    @Query(
            value = """
                    select distinct d.logOn
                    from DailyLogEntity d
                    where d.user.userId = :userId
                    """,
            countQuery = """
                    select count(distinct d.logOn)
                    from DailyLogEntity d
                    where d.user.userId = :userId
                    """
    )
    Page<LocalDate> findLogOnPageByUserId(Long userId, Pageable pageable);

    Optional<DailyLogEntity> findByDailyLogIdAndUser_UserId(Long dailyLogId, Long userId);

    @Query("""
        select d
        from DailyLogEntity d
        where d.user.userId = :userId
          and d.logOn = :logOn
        order by d.activityTime.startOn asc
    """)
    List<DailyLogEntity> findAllByUserIdAndLogOnOrderByStartOnAsc(Long userId, LocalDate logOn);

    @Query("""
        select d
        from DailyLogEntity d
        where d.user.userId = :userId
          and d.activityTime.startOn < :endOn
          and d.activityTime.endOn > :startOn
        order by d.activityTime.startOn asc
    """)
    List<DailyLogEntity> findAllByUserIdAndOverlappingTimeRangeOrderByStartOnAsc(
            Long userId,
            LocalDateTime startOn,
            LocalDateTime endOn
    );

    @Query("""
        select count(d.activityTime) > 0
        from DailyLogEntity d
        where d.user.userId = :userId
          and d.activityTime.startOn < :endOn
          and d.activityTime.endOn > :startOn
    """)
    boolean existsOverlappingTimeByUserIdAndActivityTime(Long userId, LocalDateTime startOn, LocalDateTime endOn);

    @Query("""
        select count(d.activityTime) > 0
        from DailyLogEntity d
        where d.user.userId = :userId
          and d.dailyLogId <> :excludedDailyLogId
          and d.activityTime.startOn < :endOn
          and d.activityTime.endOn > :startOn
    """)
    boolean existsOverlappingTimeByUserIdAndActivityTimeAndDailyLogIdNot(
            Long userId,
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long excludedDailyLogId
    );

    Long user(UserEntity user);
}
