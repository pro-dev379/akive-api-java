package net.akive.api.report.adapter.persistence;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.common.pagination.SortCondition;
import net.akive.api.common.pagination.SortDirection;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.report.adapter.persistence.entity.ReportEntity;
import net.akive.api.report.application.provided.dto.ReportHeaderResult;
import net.akive.api.report.application.required.ReportRepository;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
class ReportJpaAdapter implements ReportRepository {

    private static final Set<String> REPORT_PROPERTIES = Set.of("reportOn");

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
    public List<Report> findAllByUserIdAndReportTypeAndReportOnBetweenOrderByReportOnAsc(
            Long userId,
            ReportType reportType,
            LocalDate startOn,
            LocalDate endOn
    ) {
        return reportJpaRepository
                .findAllByUserIdAndReportTypeAndReportOnBetweenOrderByReportOnAsc(userId, reportType, startOn, endOn)
                .stream()
                .map(entity -> entity.to(currentTimeProvider.now()))
                .toList();
    }

    @Override
    public PageResult<ReportHeaderResult> findHeaderPageByUserId(Long userId, PageCondition pageCondition) {
        Page<ReportHeaderResult> reportHeaderPage = reportJpaRepository.findHeaderPageByUserId(
                userId,
                PageRequest.of(
                        pageCondition.getPage(),
                        pageCondition.getSize(),
                        toSort(pageCondition.getSortCondition())
                )
        );

        return PageResult.of(
                reportHeaderPage.getNumber(),
                reportHeaderPage.getSize(),
                reportHeaderPage.getTotalElements(),
                reportHeaderPage.getTotalPages(),
                reportHeaderPage.isFirst(),
                reportHeaderPage.isLast(),
                reportHeaderPage.getContent()
        );
    }

    @Override
    public List<ReportHeaderResult> findHeadersByUserIdAndReportOnBetweenOrderByReportOnAsc(
            Long userId,
            LocalDate startOn,
            LocalDate endOn
    ) {
        return reportJpaRepository.findHeadersByUserIdAndReportOnBetweenOrderByReportOnAsc(
                userId,
                startOn,
                endOn
        );
    }

    @Override
    public boolean existsByUserIdAndReportOnAndReportType(Long userId, LocalDate reportOn, ReportType reportType) {
        return reportJpaRepository.existsByUser_UserIdAndReportOnAndReportType(userId, reportOn, reportType);
    }

    @Override
    public void delete(Report report) {
        reportJpaRepository.deleteById(report.getReportId());
    }

    private Sort toSort(SortCondition sortCondition) {
        return Sort.by(
                toDirection(sortCondition.sortDirection()),
                toReportProperty(sortCondition.property())
        );
    }

    private Sort.Direction toDirection(SortDirection sortDirection) {
        return sortDirection == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private String toReportProperty(String property) {
        if (!REPORT_PROPERTIES.contains(property)) {
            throw new IllegalArgumentException("Unsupported report sort property: " + property);
        }

        return property;
    }
}
