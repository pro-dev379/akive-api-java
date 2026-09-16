package net.akive.api.report.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.report.adapter.webapi.dto.ReportTypesFindResponse;
import net.akive.api.report.application.provided.ReportFinder;
import net.akive.api.report.domain.ReportType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportApi {

    private final ReportFinder reportFinder;

    @GetMapping("/available-types")
    public ReportTypesFindResponse findAvailableReportTypes(
            @AuthenticatedUserId Long userId,
            @RequestParam LocalDate reportOn
    ) {
        List<ReportType> reportTypes = reportFinder.findAvailableReportTypes(userId, reportOn);

        return ReportTypesFindResponse.from(reportTypes);
    }
}
