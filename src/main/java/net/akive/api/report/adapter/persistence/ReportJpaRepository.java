package net.akive.api.report.adapter.persistence;

import net.akive.api.report.adapter.persistence.entity.ReportEntity;
import net.akive.api.report.application.provided.dto.ReportHeaderResult;
import net.akive.api.report.domain.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportJpaRepository extends JpaRepository<ReportEntity, Long> {

    Optional<ReportEntity> findByReportIdAndUser_UserId(Long reportId, Long userId);

    @Query("""
        select r from ReportEntity r
        where r.user.userId = :userId
            and r.reportType = :reportType
            and r.reportOn >= :from and r.reportOn <= :to
        order by r.reportOn asc
    """)
    List<ReportEntity> findAllByUserIdAndReportTypeAndReportOnBetweenOrderByReportOnAsc(
            Long userId,
            ReportType reportType,
            LocalDate from,
            LocalDate to
    );

    @Query("""
        select new net.akive.api.report.application.provided.dto.ReportHeaderResult(
            r.reportId,
            r.reportOn,
            r.reportType
        )
        from ReportEntity r
        where r.user.userId = :userId
        """)
    Page<ReportHeaderResult> findHeaderPageByUserId(Long userId, Pageable pageable);

    boolean existsByUser_UserIdAndReportOnAndReportType(Long userId, LocalDate reportOn, ReportType reportType);
}
