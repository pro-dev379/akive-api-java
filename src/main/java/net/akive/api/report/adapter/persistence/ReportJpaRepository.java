package net.akive.api.report.adapter.persistence;

import net.akive.api.report.adapter.persistence.entity.ReportEntity;
import net.akive.api.report.domain.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ReportJpaRepository extends JpaRepository<ReportEntity, Long> {

    Optional<ReportEntity> findByReportIdAndUser_UserId(Long reportId, Long userId);

    boolean existsByUser_UserIdAndReportOnAndReportType(Long userId, LocalDate reportOn, ReportType reportType);
}
