package net.akive.api.dailylog.adapter.persistence;

import net.akive.api.dailylog.adapter.persistence.entity.DailyLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    Page<LocalDate> findLogOnsByUserId(Long userId, Pageable pageable);

    @Query("""
        select d
        from DailyLogEntity d
        where d.user.userId = :userId
          and d.logOn = :logOn
        order by d.timeRange.startOn
    """)
    List<DailyLogEntity> findAllByUserIdAndLogOnOrderByStartOn(Long userId, LocalDate logOn);

    @Query("""
        select count(d) > 0
        from DailyLogEntity d
        where d.user.userId = :userId
          and d.timeRange.startOn < :endOn
          and d.timeRange.endOn > :startOn
    """)
    boolean existsOverlappingTimeByUserIdAndTimeRange(Long userId, LocalDateTime startOn, LocalDateTime endOn);

    @Query("""
        select count(d) > 0
        from DailyLogEntity d
        where d.user.userId = :userId
          and d.dailyLogId <> :excludedDailyLogId
          and d.timeRange.startOn < :endOn
          and d.timeRange.endOn > :startOn
    """)
    boolean existsOverlappingTimeByUserIdAndTimeRangeAndDailyLogIdNot(
            Long userId,
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long excludedDailyLogId
    );
}
