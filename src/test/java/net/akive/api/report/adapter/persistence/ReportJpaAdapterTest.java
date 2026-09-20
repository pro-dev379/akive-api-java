package net.akive.api.report.adapter.persistence;

import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.report.application.provided.dto.ReportHeaderResult;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportFeedback;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.report.domain.FeedbackType.HIGHLIGHT;
import static net.akive.api.report.domain.FeedbackType.IMPROVEMENT;
import static net.akive.api.report.domain.ReportType.*;
import static net.akive.api.report.domain.fixture.ReportFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

class ReportJpaAdapterTest extends RepositoryTestSupport {

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
        Report savedReport = saveReport();
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
        Report savedReport = reportRepository.save(createReport(savedUser));
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
    void 회원_식별자와_리포트_타입과_작성일_범위로_리포트를_작성일_오름차순으로_조회한다() {
        User savedUser = saveUser();
        LocalDate startOn = REPORT_ON.minusDays(2);
        LocalDate middle = REPORT_ON.minusDays(1);
        LocalDate endOn = REPORT_ON;
        List<ReportFeedback> feedbackItems = createReportFeedbackItems();
        Report oldestReport = saveReport(savedUser, startOn, WEEKLY, feedbackItems);
        Report middleReport = saveReport(savedUser, middle, WEEKLY, feedbackItems);
        Report latestReport = saveReport(savedUser, endOn, WEEKLY, feedbackItems);
        saveReport(savedUser, startOn.minusDays(1), WEEKLY);
        saveReport(savedUser, endOn.plusDays(1), WEEKLY);
        saveReport(savedUser, middle, DAILY);
        flushAndClear();

        List<Report> foundReports = reportRepository.findAllByUserIdAndReportTypeAndReportOnBetweenOrderByReportOnAsc(
                savedUser.getUserId(),
                WEEKLY,
                startOn,
                endOn
        );

        assertThat(foundReports)
                .extracting(Report::getReportId)
                .containsExactly(
                        oldestReport.getReportId(),
                        middleReport.getReportId(),
                        latestReport.getReportId()
                );
        assertThat(foundReports)
                .extracting(Report::getReportOn)
                .containsExactly(startOn, middle, endOn);
        assertThat(foundReports)
                .allSatisfy(report -> {
                    assertThat(report.getUser().getUserId()).isEqualTo(savedUser.getUserId());
                    assertThat(report.getReportType()).isEqualTo(WEEKLY);
                    assertThat(report.getFeedbackItems()).containsExactlyElementsOf(feedbackItems);
                });
    }

    @Test
    void 회원_식별자로_리포트_헤더_목록을_페이징_조회한다() {
        User savedUser = saveUser();
        reportRepository.save(createReport(savedUser, REPORT_ON.minusDays(1)));
        Report recentReport = reportRepository.save(createReport(savedUser, REPORT_ON));
        flushAndClear();

        PageResult<ReportHeaderResult> reportHeaderPage = reportRepository.findHeaderPageByUserId(
                savedUser.getUserId(),
                PageCondition.of(0, 1, "reportOn", DESC)
        );

        assertThat(reportHeaderPage.pageInfo().page()).isZero();
        assertThat(reportHeaderPage.pageInfo().size()).isEqualTo(1);
        assertThat(reportHeaderPage.pageInfo().totalElement()).isEqualTo(2);
        assertThat(reportHeaderPage.pageInfo().totalPage()).isEqualTo(2);
        assertThat(reportHeaderPage.pageInfo().first()).isTrue();
        assertThat(reportHeaderPage.pageInfo().last()).isFalse();
        assertThat(reportHeaderPage.contents())
                .containsExactly(new ReportHeaderResult(
                        recentReport.getReportId(),
                        recentReport.getReportOn(),
                        recentReport.getReportType()
                ));
    }

    @Test
    void 회원_식별자와_리포트_작성일_범위로_리포트_헤더_목록을_조회한다() {
        User savedUser = saveUser();
        LocalDate startOn = REPORT_ON.minusDays(1);
        LocalDate endOn = REPORT_ON.plusDays(1);
        Report dailyReport = saveReport(savedUser, startOn, DAILY);
        Report weeklyReport = saveReport(savedUser, REPORT_ON, WEEKLY);
        Report monthlyReport = saveReport(savedUser, REPORT_ON.plusDays(1), MONTHLY);
        saveReport(savedUser, startOn.minusDays(1), WEEKLY);
        flushAndClear();

        List<ReportHeaderResult> reportHeaders =
                reportRepository.findHeadersByUserIdAndReportOnBetweenOrderByReportOnAsc(
                        savedUser.getUserId(),
                        startOn,
                        endOn
                );

        assertThat(reportHeaders)
                .containsExactly(
                        new ReportHeaderResult(
                                dailyReport.getReportId(),
                                dailyReport.getReportOn(),
                                dailyReport.getReportType()
                        ),
                        new ReportHeaderResult(
                                weeklyReport.getReportId(),
                                weeklyReport.getReportOn(),
                                weeklyReport.getReportType()
                        ),
                        new ReportHeaderResult(
                                monthlyReport.getReportId(),
                                monthlyReport.getReportOn(),
                                monthlyReport.getReportType()
                        )
                );
    }

    @Test
    void 회원_식별자와_리포트_작성일과_타입으로_존재_여부를_조회한다() {
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
        Report savedReport = saveReport();
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
