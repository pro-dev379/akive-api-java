package net.akive.api.report.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.report.domain.ReportFixture.createYear2026SessionPeriod;
import static net.akive.api.report.domain.ReportType.*;
import static org.assertj.core.api.Assertions.assertThat;

class ReportTypePolicyTest {

    @Test
    void 일요일이_아니면_일일_리포트를_작성할_수_있다() {
        List<ReportType> reportTypes = ReportTypePolicy.findAvailableReportTypes(
                List.of(createYear2026SessionPeriod()),
                LocalDate.of(2026, 1, 5)
        );

        assertThat(reportTypes).containsExactly(DAILY);
    }

    @Test
    void 일요일이면_주간_리포트를_작성할_수_있다() {
        List<ReportType> reportTypes = ReportTypePolicy.findAvailableReportTypes(
                List.of(createYear2026SessionPeriod()),
                LocalDate.of(2026, 1, 4)
        );

        assertThat(reportTypes).containsExactly(WEEKLY);
    }

    @Test
    void 네_번째_주간_리포트_작성일이면_월간_리포트도_작성할_수_있다() {
        List<ReportType> reportTypes = ReportTypePolicy.findAvailableReportTypes(
                List.of(createYear2026SessionPeriod()),
                LocalDate.of(2026, 1, 25)
        );

        assertThat(reportTypes).containsExactly(WEEKLY, MONTHLY);
    }

    @Test
    void 여섯_번째_월간_리포트_작성일이면_반기_리포트도_작성할_수_있다() {
        List<ReportType> reportTypes = ReportTypePolicy.findAvailableReportTypes(
                List.of(createYear2026SessionPeriod()),
                LocalDate.of(2026, 6, 14)
        );

        assertThat(reportTypes).containsExactly(WEEKLY, MONTHLY, BIANNUALLY);
    }

    @Test
    void 두_번째_반기_리포트_작성일이면_연간_리포트도_작성할_수_있다() {
        List<ReportType> reportTypes = ReportTypePolicy.findAvailableReportTypes(
                List.of(createYear2026SessionPeriod()),
                LocalDate.of(2026, 11, 29)
        );

        assertThat(reportTypes).containsExactly(WEEKLY, MONTHLY, BIANNUALLY, ANNUALLY);
    }
}
