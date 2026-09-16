package net.akive.api.report.adapter.persistence;

import net.akive.api.report.application.required.ReportRepository;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportFeedback;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static net.akive.api.report.domain.FeedbackType.HIGHLIGHT;
import static net.akive.api.report.domain.FeedbackType.IMPROVEMENT;
import static net.akive.api.report.domain.ReportFixture.REPORT_ON;
import static net.akive.api.report.domain.ReportFixture.createReportFeedback;
import static net.akive.api.report.domain.ReportType.WEEKLY;
import static org.assertj.core.api.Assertions.assertThat;

class ReportJpaAdapterTest extends RepositoryTestSupport {

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    ReportJpaRepository reportJpaRepository;

    @Test
    void 리포트를_저장한다() {
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
    void 회원_식별자와_리포트_작성일과_리포트_타입으로_존재_여부를_조회한다() {
        User savedUser = saveUser();
        Report report = Report.create(
                savedUser,
                REPORT_ON,
                WEEKLY,
                List.of(createReportFeedback(HIGHLIGHT)),
                currentTimeProvider.now()
        );
        reportRepository.save(report);
        flushAndClear();

        boolean exists = reportRepository.existsByUserIdAndReportOnAndReportType(
                savedUser.getUserId(),
                REPORT_ON,
                WEEKLY
        );

        assertThat(exists).isTrue();
    }
}
