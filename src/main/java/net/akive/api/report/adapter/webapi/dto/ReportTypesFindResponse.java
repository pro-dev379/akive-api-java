package net.akive.api.report.adapter.webapi.dto;

import net.akive.api.report.domain.ReportType;

import java.util.List;

public record ReportTypesFindResponse(List<ReportTypeResponse> reportTypes) {

    public static ReportTypesFindResponse from(List<ReportType> reportTypes) {
        List<ReportTypeResponse> reportTypeResponses =
                reportTypes.stream().map(ReportTypeResponse::from).toList();

        return new ReportTypesFindResponse(reportTypeResponses);
    }

    public record ReportTypeResponse(String code, String label) {

        public static ReportTypeResponse from(ReportType reportType) {
            return new ReportTypeResponse(reportType.name(), reportType.getLabel());
        }
    }
}
