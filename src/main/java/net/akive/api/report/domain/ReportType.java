package net.akive.api.report.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {

    DAILY("일일 리포트"),
    WEEKLY("주간 리포트"),
    MONTHLY("월간 리포트"),
    BIANNUALLY("반기 리포트"),
    ANNUALLY("연간 리포트");

    private final String label;
}
