package net.akive.api.report.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.plan.domain.value.SessionPeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_REPORT_TYPE;
import static net.akive.api.report.domain.ReportFixture.createYear2026SessionPeriod;
import static net.akive.api.report.domain.ReportType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ReportScheduleTest {

    @Test
    void 일요일이_아니면_일일_리포트를_작성할_수_있다() {
        ReportSchedule reportSchedule = ReportSchedule.from(List.of(createYear2026SessionPeriod()));

        List<ReportType> reportTypes = reportSchedule.findAvailableReportTypes(LocalDate.of(2026, 1, 5));

        assertThat(reportTypes).containsExactly(DAILY);
    }

    @Test
    void 일요일이면_주간_리포트를_작성할_수_있다() {
        ReportSchedule reportSchedule = ReportSchedule.from(List.of(createYear2026SessionPeriod()));

        List<ReportType> reportTypes = reportSchedule.findAvailableReportTypes(LocalDate.of(2026, 1, 4));

        assertThat(reportTypes).containsExactly(WEEKLY);
    }

    @Test
    void 네_번째_주간_리포트_작성일이면_월간_리포트도_작성할_수_있다() {
        ReportSchedule reportSchedule = ReportSchedule.from(List.of(createYear2026SessionPeriod()));

        List<ReportType> reportTypes = reportSchedule.findAvailableReportTypes(LocalDate.of(2026, 1, 25));

        assertThat(reportTypes).containsExactly(WEEKLY, MONTHLY);
    }

    @Test
    void 여섯_번째_월간_리포트_작성일이면_반기_리포트도_작성할_수_있다() {
        ReportSchedule reportSchedule = ReportSchedule.from(List.of(createYear2026SessionPeriod()));

        List<ReportType> reportTypes = reportSchedule.findAvailableReportTypes(LocalDate.of(2026, 6, 14));

        assertThat(reportTypes).containsExactly(WEEKLY, MONTHLY, BIANNUALLY);
    }

    @Test
    void 두_번째_반기_리포트_작성일이면_연간_리포트도_작성할_수_있다() {
        ReportSchedule reportSchedule = ReportSchedule.from(List.of(createYear2026SessionPeriod()));

        List<ReportType> reportTypes = reportSchedule.findAvailableReportTypes(LocalDate.of(2026, 11, 29));

        assertThat(reportTypes).containsExactly(WEEKLY, MONTHLY, BIANNUALLY, ANNUALLY);
    }

    @Test
    void 일일_리포트의_커버_기간을_조회한다() {
        ReportSchedule reportSchedule = ReportSchedule.from(List.of(createYear2026SessionPeriod()));

        ReportCoveragePeriod coveragePeriod = reportSchedule.findCoveragePeriod(
                LocalDate.of(2026, 1, 5),
                DAILY
        );

        assertThat(coveragePeriod).isEqualTo(new ReportCoveragePeriod(
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 5)
        ));
    }

    @Test
    void 주간_리포트의_커버_기간을_조회한다() {
        ReportSchedule reportSchedule = ReportSchedule.from(List.of(createYear2026SessionPeriod()));

        ReportCoveragePeriod coveragePeriod = reportSchedule.findCoveragePeriod(
                LocalDate.of(2026, 1, 4),
                WEEKLY
        );

        assertThat(coveragePeriod).isEqualTo(new ReportCoveragePeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 4)
        ));
    }

    @Test
    void 월간_리포트의_커버_기간을_조회한다() {
        ReportSchedule reportSchedule = ReportSchedule.from(List.of(createYear2026SessionPeriod()));

        ReportCoveragePeriod coveragePeriod = reportSchedule.findCoveragePeriod(
                LocalDate.of(2026, 1, 25),
                MONTHLY
        );

        assertThat(coveragePeriod).isEqualTo(new ReportCoveragePeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 25)
        ));
    }

    @Test
    void 반기_리포트의_커버_기간을_조회한다() {
        ReportSchedule reportSchedule = ReportSchedule.from(List.of(createYear2026SessionPeriod()));

        ReportCoveragePeriod coveragePeriod = reportSchedule.findCoveragePeriod(
                LocalDate.of(2026, 6, 14),
                BIANNUALLY
        );

        assertThat(coveragePeriod).isEqualTo(new ReportCoveragePeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 14)
        ));
    }

    @Test
    void 연간_리포트의_커버_기간을_조회한다() {
        ReportSchedule reportSchedule = ReportSchedule.from(List.of(createYear2026SessionPeriod()));

        ReportCoveragePeriod coveragePeriod = reportSchedule.findCoveragePeriod(
                LocalDate.of(2026, 11, 29),
                ANNUALLY
        );

        assertThat(coveragePeriod).isEqualTo(new ReportCoveragePeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 11, 29)
        ));
    }

    @Test
    void 리포트_작성일에_맞지_않는_리포트_타입으로_커버_기간을_조회하면_예외가_발생한다() {
        ReportSchedule reportSchedule = ReportSchedule.from(List.of(createYear2026SessionPeriod()));

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> reportSchedule.findCoveragePeriod(
                        LocalDate.of(2026, 1, 5),
                        WEEKLY
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_REPORT_TYPE);
    }

    @Test
    void 오버랩되는_세션_기간이_있어도_리포트_작성_가능한_리포트_타입과_커버_기간을_조회할_수_있다() {
        List<SessionPeriod> sessionPeriods = List.of(
                new SessionPeriod(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)),
                new SessionPeriod(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 31))
        );
        LocalDate reportOn = LocalDate.of(2026, 10, 25);
        ReportSchedule reportSchedule = ReportSchedule.from(sessionPeriods);

        List<ReportType> reportTypes = reportSchedule.findAvailableReportTypes(reportOn);
        ReportCoveragePeriod coveragePeriod = reportSchedule.findCoveragePeriod(
                reportOn,
                MONTHLY
        );

        assertThat(reportTypes).containsExactly(WEEKLY, MONTHLY);
        assertThat(coveragePeriod).isEqualTo(new ReportCoveragePeriod(
                LocalDate.of(2026, 10, 1),
                reportOn
        ));
    }
}
