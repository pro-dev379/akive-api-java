package net.akive.api.report.domain;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.requireStartNotAfterEnd;

public record ReportCoveragePeriod(
        LocalDate startOn,
        LocalDate endOn
) {

    public ReportCoveragePeriod {
        requireStartNotAfterEnd(startOn, endOn);
    }
}
