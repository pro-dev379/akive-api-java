package net.akive.api.report.adapter.persistence;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.report.adapter.persistence.entity.ReportEntity;
import net.akive.api.report.application.required.ReportRepository;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportType;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class ReportJpaAdapter implements ReportRepository {

    private final ReportJpaRepository reportJpaRepository;
    private final CurrentTimeProvider currentTimeProvider;
    private final EntityManager entityManager;

    @Override
    public Report save(Report report) {
        if (report.getReportId() == null) {
            return reportJpaRepository.save(ReportEntity.from(report)).to(currentTimeProvider.now());
        }

        ReportEntity reportEntity = reportJpaRepository.findById(report.getReportId()).orElseThrow();

        reportEntity.clearFeedbackItems();
        entityManager.flush();

        reportEntity.updateFeedbackItems(report.getFeedbackItems());

        return reportEntity.to(currentTimeProvider.now());
    }

    @Override
    public Optional<Report> findByReportIdAndUserId(Long reportId, Long userId) {
        return reportJpaRepository.findByReportIdAndUser_UserId(reportId, userId)
                .map(entity -> entity.to(currentTimeProvider.now()));
    }

    @Override
    public boolean existsByUserIdAndReportOnAndReportType(Long userId, LocalDate reportOn, ReportType reportType) {
        return reportJpaRepository.existsByUser_UserIdAndReportOnAndReportType(userId, reportOn, reportType);
    }
}
