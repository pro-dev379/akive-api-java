package net.akive.api.report.domain;

import net.akive.api.plan.domain.value.SessionPeriod;

import java.time.LocalDate;

public class ReportFixture {

    public static final LocalDate REPORT_ON = LocalDate.of(2026, 1, 25);

    public static SessionPeriod createYear2026SessionPeriod() {
        return new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }
}
