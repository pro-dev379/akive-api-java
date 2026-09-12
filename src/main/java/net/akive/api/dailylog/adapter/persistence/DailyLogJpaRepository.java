package net.akive.api.dailylog.adapter.persistence;

import net.akive.api.dailylog.adapter.persistence.entity.DailyLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
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

    List<DailyLogEntity> findAllByUser_UserIdAndLogOnOrderByStartOn(Long userId, LocalDate logOn);
}
