package net.akive.api.report.domain;

import lombok.NoArgsConstructor;
import net.akive.api.plan.domain.value.SessionPeriod;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static java.time.DayOfWeek.SUNDAY;
import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotEmpty;
import static net.akive.api.report.domain.ReportType.*;

@NoArgsConstructor(access = PRIVATE)
public final class ReportTypePolicy {

    private static final int WEEKLY_REPORT_COUNT_PER_MONTHLY_REPORT = 4;
    private static final int MONTHLY_REPORT_COUNT_PER_BIANNUAL_REPORT = 6;
    private static final int BIANNUAL_REPORT_COUNT_PER_ANNUAL_REPORT = 2;

    public static List<ReportType> findAvailableReportTypes(
            List<SessionPeriod> sessionPeriods,
            LocalDate reportOn
    ) {
        requireNotEmpty(sessionPeriods);
        requireNonNull(reportOn);

        List<ReportType> availableReportTypes = new ArrayList<>();

        if (reportOn.getDayOfWeek() == SUNDAY) {
            availableReportTypes.add(WEEKLY);
        } else {
            availableReportTypes.add(DAILY);
        }

        List<LocalDate> weeklyReportOns = findWeeklyReportOns(sessionPeriods);
        List<LocalDate> monthlyReportOns = findCompletedGroupLastReportOns(
                weeklyReportOns,
                WEEKLY_REPORT_COUNT_PER_MONTHLY_REPORT
        );
        if (monthlyReportOns.contains(reportOn)) {
            availableReportTypes.add(MONTHLY);
        }

        List<LocalDate> biannualReportOns = findCompletedGroupLastReportOns(
                monthlyReportOns,
                MONTHLY_REPORT_COUNT_PER_BIANNUAL_REPORT
        );
        if (biannualReportOns.contains(reportOn)) {
            availableReportTypes.add(BIANNUALLY);
        }

        List<LocalDate> annualReportOns = findCompletedGroupLastReportOns(
                biannualReportOns,
                BIANNUAL_REPORT_COUNT_PER_ANNUAL_REPORT
        );
        if (annualReportOns.contains(reportOn)) {
            availableReportTypes.add(ANNUALLY);
        }

        return List.copyOf(availableReportTypes);
    }

    private static List<LocalDate> findWeeklyReportOns(List<SessionPeriod> sessionPeriods) {
        TreeSet<LocalDate> weeklyReportOns = new TreeSet<>();

        for (SessionPeriod sessionPeriod : sessionPeriods) {
            LocalDate weeklyReportOn = sessionPeriod.startOn()
                    .with(TemporalAdjusters.nextOrSame(SUNDAY));

            while (!weeklyReportOn.isAfter(sessionPeriod.endOn())) {
                weeklyReportOns.add(weeklyReportOn);
                weeklyReportOn = weeklyReportOn.plusWeeks(1);
            }
        }

        return List.copyOf(weeklyReportOns);
    }

    private static List<LocalDate> findCompletedGroupLastReportOns(
            List<LocalDate> reportOns,
            int groupSize
    ) {
        List<LocalDate> lastReportOns = new ArrayList<>();

        for (int lastIndex = groupSize - 1; lastIndex < reportOns.size(); lastIndex += groupSize) {
            lastReportOns.add(reportOns.get(lastIndex));
        }

        return List.copyOf(lastReportOns);
    }
}
