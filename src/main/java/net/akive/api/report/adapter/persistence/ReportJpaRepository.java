package net.akive.api.report.adapter.persistence;

import net.akive.api.report.adapter.persistence.entity.ReportEntity;
import net.akive.api.report.application.provided.dto.ReportHeaderResult;
import net.akive.api.report.domain.ReportType;
import net.akive.api.user.adapter.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface ReportJpaRepository extends JpaRepository<ReportEntity, Long> {

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

    Optional<ReportEntity> findByReportIdAndUser_UserId(Long reportId, Long userId);

    boolean existsByUser_UserIdAndReportOnAndReportType(Long userId, LocalDate reportOn, ReportType reportType);

    Long user(UserEntity user);
}
