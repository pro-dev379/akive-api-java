package net.akive.api.report.adapter.persistence;

import net.akive.api.report.adapter.persistence.entity.ReportEntity;
import net.akive.api.report.domain.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ReportJpaRepository extends JpaRepository<ReportEntity, Long> {

    boolean existsByUser_UserIdAndReportOnAndReportType(Long userId, LocalDate reportOn, ReportType reportType);
}
