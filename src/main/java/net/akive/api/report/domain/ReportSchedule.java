package net.akive.api.report.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.plan.domain.value.SessionPeriod;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static java.time.DayOfWeek.SUNDAY;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotEmpty;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_REPORT_TYPE;
import static net.akive.api.report.domain.ReportType.*;

public final class ReportSchedule {

    private static final int WEEKLY_REPORT_COUNT_PER_MONTHLY_REPORT = 4;
    private static final int MONTHLY_REPORT_COUNT_PER_BIANNUAL_REPORT = 6;
    private static final int BIANNUAL_REPORT_COUNT_PER_ANNUAL_REPORT = 2;

    private final List<SessionPeriod> sessionPeriods;
    private final Map<ReportType, List<ReportCoveragePeriod>> coveragePeriodsByType;

    private ReportSchedule(List<SessionPeriod> sessionPeriods) {
        this.sessionPeriods = sessionPeriods;
        this.coveragePeriodsByType = createCoveragePeriodsByType(sessionPeriods);
    }

    public static ReportSchedule from(List<SessionPeriod> sessionPeriods) {
        requireNotEmpty(sessionPeriods);

        return new ReportSchedule(List.copyOf(sessionPeriods));
    }

    public List<ReportType> findAvailableReportTypes(LocalDate reportOn) {
        requireNonNull(reportOn);

        List<ReportType> availableReportTypes = new ArrayList<>();

        for (ReportType reportType : ReportType.values()) {
            if (hasReportOn(reportOn, reportType)) {
                availableReportTypes.add(reportType);
            }
        }

        return List.copyOf(availableReportTypes);
    }

    public ReportCoveragePeriod findCoveragePeriod(LocalDate reportOn, ReportType reportType) {
        requireNonNull(reportOn);
        requireNonNull(reportType);

        if (reportType == DAILY) {
            return findDailyCoveragePeriod(reportOn);
        }

        return findCoveragePeriodEndingOn(getCoveragePeriods(reportType), reportOn);
    }

    private boolean hasReportOn(LocalDate reportOn, ReportType reportType) {
        if (reportType == DAILY) {
            return isDailyReportOn(reportOn);
        }

        return hasCoveragePeriodEndingOn(getCoveragePeriods(reportType), reportOn);
    }

    private ReportCoveragePeriod findDailyCoveragePeriod(LocalDate reportOn) {
        if (isDailyReportOn(reportOn)) {
            return new ReportCoveragePeriod(reportOn, reportOn);
        }

        throw invalidReportType();
    }

    private boolean isDailyReportOn(LocalDate reportOn) {
        return reportOn.getDayOfWeek() != SUNDAY && isWithinSessionPeriods(reportOn);
    }

    private boolean isWithinSessionPeriods(LocalDate reportOn) {
        return sessionPeriods.stream()
                .anyMatch(sessionPeriod -> !sessionPeriod.startOn().isAfter(reportOn)
                        && !sessionPeriod.endOn().isBefore(reportOn));
    }

    private List<ReportCoveragePeriod> getCoveragePeriods(ReportType reportType) {
        List<ReportCoveragePeriod> coveragePeriods = coveragePeriodsByType.get(reportType);

        if (coveragePeriods == null) {
            throw invalidReportType();
        }

        return coveragePeriods;
    }

    private static Map<ReportType, List<ReportCoveragePeriod>> createCoveragePeriodsByType(
            List<SessionPeriod> sessionPeriods
    ) {
        List<ReportCoveragePeriod> weeklyCoveragePeriods = findWeeklyCoveragePeriods(sessionPeriods);
        List<ReportCoveragePeriod> monthlyCoveragePeriod = findGroupedCoveragePeriods(
                weeklyCoveragePeriods,
                WEEKLY_REPORT_COUNT_PER_MONTHLY_REPORT
        );
        List<ReportCoveragePeriod> biannualCoveragePeriods = findGroupedCoveragePeriods(
                monthlyCoveragePeriod,
                MONTHLY_REPORT_COUNT_PER_BIANNUAL_REPORT
        );
        List<ReportCoveragePeriod> annualCoveragePeriods = findGroupedCoveragePeriods(
                biannualCoveragePeriods,
                BIANNUAL_REPORT_COUNT_PER_ANNUAL_REPORT
        );

        Map<ReportType, List<ReportCoveragePeriod>> coveragePeriodsByType = new EnumMap<>(ReportType.class);
        coveragePeriodsByType.put(WEEKLY, weeklyCoveragePeriods);
        coveragePeriodsByType.put(MONTHLY, monthlyCoveragePeriod);
        coveragePeriodsByType.put(BIANNUALLY, biannualCoveragePeriods);
        coveragePeriodsByType.put(ANNUALLY, annualCoveragePeriods);

        return Map.copyOf(coveragePeriodsByType);
    }

    private static ReportCoveragePeriod findCoveragePeriodEndingOn(
            List<ReportCoveragePeriod> coveragePeriods,
            LocalDate reportOn
    ) {
        return coveragePeriods.stream()
                .filter(period -> period.endOn().equals(reportOn))
                .findFirst()
                .orElseThrow(ReportSchedule::invalidReportType);
    }

    private static boolean hasCoveragePeriodEndingOn(
            List<ReportCoveragePeriod> coveragePeriods,
            LocalDate reportOn
    ) {
        return coveragePeriods.stream()
                .anyMatch(coveragePeriod -> coveragePeriod.endOn().equals(reportOn));
    }

    private static List<ReportCoveragePeriod> findGroupedCoveragePeriods(
            List<ReportCoveragePeriod> coveragePeriods,
            int groupSize
    ) {
        List<ReportCoveragePeriod> groupedPeriods = new ArrayList<>();

        for (int lastIndex = groupSize - 1; lastIndex < coveragePeriods.size(); lastIndex += groupSize) {
            ReportCoveragePeriod firstPeriod = coveragePeriods.get(lastIndex - groupSize + 1);
            ReportCoveragePeriod lastPeriod = coveragePeriods.get(lastIndex);

            groupedPeriods.add(new ReportCoveragePeriod(firstPeriod.startOn(), lastPeriod.endOn()));
        }

        return List.copyOf(groupedPeriods);
    }

    private static List<ReportCoveragePeriod> findWeeklyCoveragePeriods(List<SessionPeriod> sessionPeriods) {
        TreeMap<LocalDate, ReportCoveragePeriod> reportPeriodByEndOn = new TreeMap<>();

        sessionPeriods.stream()
                .sorted(Comparator.comparing(SessionPeriod::startOn).thenComparing(SessionPeriod::endOn))
                .flatMap(sessionPeriod -> findWeeklyCoveragePeriods(sessionPeriod).stream())
                .forEach(period -> reportPeriodByEndOn.putIfAbsent(period.endOn(), period));

        return List.copyOf(reportPeriodByEndOn.values());
    }

    private static List<ReportCoveragePeriod> findWeeklyCoveragePeriods(SessionPeriod sessionPeriod) {
        List<ReportCoveragePeriod> coveragePeriods = new ArrayList<>();
        LocalDate startOn = sessionPeriod.startOn();
        LocalDate endOn = startOn.with(TemporalAdjusters.nextOrSame(SUNDAY));

        while (!endOn.isAfter(sessionPeriod.endOn())) {
            coveragePeriods.add(new ReportCoveragePeriod(startOn, endOn));
            startOn = endOn.plusDays(1);
            endOn = endOn.plusWeeks(1);
        }

        return List.copyOf(coveragePeriods);
    }

    private static RuntimeException invalidReportType() {
        return new BusinessRuleViolationException(INVALID_REPORT_TYPE);
    }
}
