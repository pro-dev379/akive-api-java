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
        order by d.focusTime.startOn
    """)
    List<DailyLogEntity> findAllByUserIdAndLogOnOrderByStartOn(Long userId, LocalDate logOn);

    @Query("""
        select count(d) > 0
        from DailyLogEntity d
        where d.user.userId = :userId
          and d.focusTime.startOn < :endOn
          and d.focusTime.endOn > :startOn
    """)
    boolean existsOverlappingTimeByUserIdAndFocusTime(Long userId, LocalDateTime startOn, LocalDateTime endOn);

    @Query("""
        select count(d) > 0
        from DailyLogEntity d
        where d.user.userId = :userId
          and d.dailyLogId <> :excludedDailyLogId
          and d.focusTime.startOn < :endOn
          and d.focusTime.endOn > :startOn
    """)
    boolean existsOverlappingTimeByUserIdAndFocusTimeAndDailyLogIdNot(
            Long userId,
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long excludedDailyLogId
    );

    Long user(UserEntity user);
}
