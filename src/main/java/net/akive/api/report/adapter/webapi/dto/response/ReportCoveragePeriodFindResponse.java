package net.akive.api.report.adapter.webapi.dto.response;

import net.akive.api.report.domain.ReportCoveragePeriod;

import java.time.LocalDate;

public record ReportCoveragePeriodFindResponse(
        LocalDate startOn,
        LocalDate endOn
) {

    public static ReportCoveragePeriodFindResponse from(ReportCoveragePeriod coveragePeriod) {
        return new ReportCoveragePeriodFindResponse(coveragePeriod.startOn(), coveragePeriod.endOn());
    }
}
