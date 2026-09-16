package net.akive.api.report.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.report.adapter.persistence.entity.ReportEntity;
import net.akive.api.report.application.required.ReportRepository;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportType;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
class ReportJpaAdapter implements ReportRepository {

    private final ReportJpaRepository reportJpaRepository;
    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public Report save(Report report) {
        return reportJpaRepository.save(ReportEntity.from(report)).to(currentTimeProvider.now());
    }

    @Override
    public boolean existsByUserIdAndReportOnAndReportType(Long userId, LocalDate reportOn, ReportType reportType) {
        return reportJpaRepository.existsByUser_UserIdAndReportOnAndReportType(userId, reportOn, reportType);
    }
}
