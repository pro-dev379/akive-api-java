package net.akive.api.report.adapter.webapi.dto.response;

import net.akive.api.common.pagination.PageInfo;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.report.application.provided.dto.ReportHeaderResult;
import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;
import java.util.List;

public record ReportHeaderPageFindResponse(
        PageInfo pageInfo,
        List<ReportHeaderResponse> reportHeaders
) {

    public static ReportHeaderPageFindResponse from(PageResult<ReportHeaderResult> reportHeaderPage) {
        return new ReportHeaderPageFindResponse(
                reportHeaderPage.pageInfo(),
                reportHeaderPage.contents().stream()
                        .map(ReportHeaderResponse::from)
                        .toList()
        );
    }

    public record ReportHeaderResponse(
            Long reportId,
            LocalDate reportOn,
            ReportType reportType
    ) {

        public static ReportHeaderResponse from(ReportHeaderResult reportHeader) {
            return new ReportHeaderResponse(
                    reportHeader.reportId(),
                    reportHeader.reportOn(),
                    reportHeader.reportType()
            );
        }
    }
}
