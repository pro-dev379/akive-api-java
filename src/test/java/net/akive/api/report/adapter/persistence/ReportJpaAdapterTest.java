package net.akive.api.report.adapter.persistence;

import net.akive.api.report.application.required.ReportRepository;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportFeedback;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static net.akive.api.report.domain.FeedbackType.HIGHLIGHT;
import static net.akive.api.report.domain.FeedbackType.IMPROVEMENT;
import static net.akive.api.report.domain.ReportFixture.*;
import static net.akive.api.report.domain.ReportType.WEEKLY;
import static org.assertj.core.api.Assertions.assertThat;

class ReportJpaAdapterTest extends RepositoryTestSupport {

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    ReportJpaRepository reportJpaRepository;

    @Test
    void 신규_리포트를_저장한다() {
        User savedUser = saveUser();
        ReportFeedback firstFeedback = createReportFeedback(HIGHLIGHT);
        ReportFeedback secondFeedback = createReportFeedback(IMPROVEMENT);
        Report report = Report.create(
                savedUser,
                REPORT_ON,
                WEEKLY,
                List.of(firstFeedback, secondFeedback),
                currentTimeProvider.now()
        );

        Report savedReport = reportRepository.save(report);
        flushAndClear();

        Report foundReport = reportJpaRepository.findById(savedReport.getReportId())
                .orElseThrow()
                .to(currentTimeProvider.now());

        assertThat(foundReport.getReportId()).isEqualTo(savedReport.getReportId());
        assertThat(foundReport.getUser().getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(foundReport.getReportOn()).isEqualTo(REPORT_ON);
        assertThat(foundReport.getReportType()).isEqualTo(WEEKLY);
        assertThat(foundReport.getFeedbackItems()).containsExactly(firstFeedback, secondFeedback);
    }

    @Test
    void 저장된_리포트를_다시_저장할_때_피드백_목록을_모두_교체해서_저장한다() {
        User savedUser = saveUser();
        Report report = createReport(savedUser);
        Report savedReport = reportRepository.save(report);
        flushAndClear();

        List<ReportFeedback> targetFeedbackItems = createTargetReportFeedbackItems();
        savedReport.updateFeedbackItems(targetFeedbackItems);

        reportRepository.save(savedReport);
        flushAndClear();

        Report foundReport = reportJpaRepository.findById(savedReport.getReportId())
                .orElseThrow()
                .to(currentTimeProvider.now());

        assertThat(foundReport.getFeedbackItems())
                .containsExactlyElementsOf(targetFeedbackItems);
    }

    @Test
    void 리포트_식별자와_회원_식별자로_리포트를_조회한다() {
        User savedUser = saveUser();
        Report report = createReport(savedUser);
        Report savedReport = reportRepository.save(report);
        flushAndClear();

        Optional<Report> foundReport = reportRepository.findByReportIdAndUserId(
                savedReport.getReportId(),
                savedUser.getUserId()
        );

        assertThat(foundReport).isPresent();
        assertThat(foundReport.get().getReportId()).isEqualTo(savedReport.getReportId());
        assertThat(foundReport.get().getUser().getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(foundReport.get().getReportOn()).isEqualTo(savedReport.getReportOn());
        assertThat(foundReport.get().getReportType()).isEqualTo(savedReport.getReportType());
        assertThat(foundReport.get().getFeedbackItems()).containsExactlyElementsOf(savedReport.getFeedbackItems());
    }

    @Test
    void 회원_식별자와_리포트_작성일과_리포트_타입으로_존재_여부를_조회한다() {
        User savedUser = saveUser();
        Report savedReport = reportRepository.save(createReport(savedUser));
        flushAndClear();

        boolean exists = reportRepository.existsByUserIdAndReportOnAndReportType(
                savedUser.getUserId(),
                savedReport.getReportOn(),
                savedReport.getReportType()
        );

        assertThat(exists).isTrue();
    }

    @Test
    void 리포트를_삭제한다() {
        User savedUser = saveUser();
        Report savedReport = reportRepository.save(createReport(savedUser));
        flushAndClear();

        assertThat(countReportFeedbacksByReportId(savedReport.getReportId()))
                .isEqualTo(savedReport.getFeedbackItems().size());

        reportRepository.delete(savedReport);
        flushAndClear();

        assertThat(reportJpaRepository.findById(savedReport.getReportId())).isEmpty();
        assertThat(countReportFeedbacksByReportId(savedReport.getReportId())).isZero();
    }

    private long countReportFeedbacksByReportId(Long reportId) {
        return entityManager
                .createQuery("""
                        select count(reportFeedback)
                        from ReportFeedbackEntity reportFeedback
                        where reportFeedback.report.reportId = :reportId
                        """, Long.class)
                .setParameter("reportId", reportId)
                .getSingleResult();
    }
}
