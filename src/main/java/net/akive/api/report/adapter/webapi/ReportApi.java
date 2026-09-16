package net.akive.api.report.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.report.adapter.webapi.dto.request.ReportCreateRequest;
import net.akive.api.report.adapter.webapi.dto.response.ReportCreateResponse;
import net.akive.api.report.adapter.webapi.dto.response.ReportTypesFindResponse;
import net.akive.api.report.application.provided.ReportCreator;
import net.akive.api.report.application.provided.ReportFinder;
import net.akive.api.report.domain.ReportType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportApi {

    private final ReportFinder reportFinder;
    private final ReportCreator reportCreator;

    private final CurrentTimeProvider currentTimeProvider;

    @PostMapping
    @ResponseStatus(CREATED)
    public ReportCreateResponse create(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody ReportCreateRequest request
    ) {
        LocalDateTime requestedAt = currentTimeProvider.now();

        return new ReportCreateResponse(reportCreator.create(request.to(userId, requestedAt)));
    }

    @GetMapping("/available-types")
    public ReportTypesFindResponse findAvailableReportTypes(
            @AuthenticatedUserId Long userId,
            @RequestParam LocalDate reportOn
    ) {
        List<ReportType> reportTypes = reportFinder.findAvailableReportTypes(userId, reportOn);

        return ReportTypesFindResponse.from(reportTypes);
    }
}
