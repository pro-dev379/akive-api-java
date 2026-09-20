package net.akive.api.report.application.service.fixture;

import net.akive.api.report.application.provided.dto.ReportHeaderResult;
import net.akive.api.report.application.provided.dto.ReportWritingStatusResult;
import net.akive.api.report.domain.ReportCoveragePeriod;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.report.domain.ReportType.*;
import static net.akive.api.report.domain.fixture.ReportFixture.*;

public class ReportQueryFixture {

    private ReportQueryFixture() {
    }

    public static ReportWritingStatusResult createDailyWritingStatus() {
        return new ReportWritingStatusResult(
                DAILY,
                REPORT_ON.minusDays(1),
                new ReportCoveragePeriod(
                        REPORT_ON.minusDays(1),
                        REPORT_ON.minusDays(1)
                ),
                true
        );
    }

    public static ReportWritingStatusResult createWeeklyWritingStatus() {
        return new ReportWritingStatusResult(
                WEEKLY,
                REPORT_ON,
                new ReportCoveragePeriod(
                        REPORT_ON.minusDays(6),
                        REPORT_ON
                ),
                true
        );
    }

    public static ReportWritingStatusResult createMonthlyWritingStatus() {
        return new ReportWritingStatusResult(
                MONTHLY,
                REPORT_ON.plusWeeks(3),
                new ReportCoveragePeriod(
                        REPORT_ON.minusDays(6),
                        REPORT_ON.plusWeeks(3)
                ),
                false
        );
    }

    public static List<ReportHeaderResult> createWrittenReportHeaders() {
        return List.of(
                new ReportHeaderResult(
                        REPORT_ID,
                        LocalDate.of(2026, 1, 20),
                        DAILY
                ),
                new ReportHeaderResult(
                        ANOTHER_REPORT_ID,
                        LocalDate.of(2026, 1, 18),
                        WEEKLY
                ),
                new ReportHeaderResult(
                        ANOTHER_REPORT_ID + 1,
                        LocalDate.of(2026, 1, 25),
                        MONTHLY
                )
        );
    }
}
