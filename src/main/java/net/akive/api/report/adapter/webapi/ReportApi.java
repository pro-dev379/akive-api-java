package net.akive.api.report.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.report.adapter.webapi.dto.request.ReportCreateRequest;
import net.akive.api.report.adapter.webapi.dto.request.ReportFeedbackUpdateRequest;
import net.akive.api.report.adapter.webapi.dto.response.*;
import net.akive.api.report.application.provided.ReportCreator;
import net.akive.api.report.application.provided.ReportFinder;
import net.akive.api.report.application.provided.dto.ReportFeedbackHistoryFindQuery;
import net.akive.api.report.application.provided.dto.ReportHeaderResult;
import net.akive.api.report.domain.ReportType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static net.akive.api.common.pagination.SortDirection.DESC;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

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

    @GetMapping("/{reportId}")
    public ReportFindResponse find(@AuthenticatedUserId Long userId, @PathVariable Long reportId) {
        return ReportFindResponse.from(reportFinder.find(reportId, userId));
    }

    @DeleteMapping("/{reportId}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@AuthenticatedUserId Long userId, @PathVariable Long reportId) {
        reportCreator.delete(reportId, userId);
    }

    @PutMapping("/{reportId}/feedback")
    @ResponseStatus(NO_CONTENT)
    public void updateFeedback(
            @AuthenticatedUserId Long userId,
            @PathVariable Long reportId,
            @Validated @RequestBody ReportFeedbackUpdateRequest request
    ) {
        reportCreator.updateFeedbackItems(request.to(reportId, userId));
    }

    @GetMapping("/headers")
    public ReportHeaderPageFindResponse findRecentHeaders(
            @AuthenticatedUserId Long userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        PageResult<ReportHeaderResult> reportHeaderPage =
                reportFinder.findHeaderPage(userId, PageCondition.of(page, size, "reportOn", DESC));

        return ReportHeaderPageFindResponse.from(reportHeaderPage);
    }

    @GetMapping("/feedback-history")
    public ReportFeedbackHistoryFindResponse findFeedbackHistory(
            @AuthenticatedUserId Long userId,
            @RequestParam ReportType reportType,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        ReportFeedbackHistoryFindQuery query = new ReportFeedbackHistoryFindQuery(userId, reportType, from, to);

        return ReportFeedbackHistoryFindResponse.from(reportFinder.findFeedbackHistory(query));
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
