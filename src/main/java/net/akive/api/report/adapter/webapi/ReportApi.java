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
import net.akive.api.report.application.provided.dto.ReportCoveragePeriodFindQuery;
import net.akive.api.report.application.provided.dto.ReportFeedbackHistoryFindQuery;
import net.akive.api.report.application.provided.dto.ReportHeaderResult;
import net.akive.api.report.domain.ReportType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.common.time.ServiceDayPolicy.toKoreanDate;
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
        LocalDate currentDate = toKoreanDate(currentTimeProvider.now());

        return new ReportCreateResponse(reportCreator.create(request.to(userId, currentDate)));
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
            @RequestParam LocalDate startOn,
            @RequestParam LocalDate endOn
    ) {
        ReportFeedbackHistoryFindQuery query = new ReportFeedbackHistoryFindQuery(userId, reportType, startOn, endOn);

        return ReportFeedbackHistoryFindResponse.from(reportFinder.findFeedbackHistory(query));
    }

    @GetMapping("/coverage-period")
    public ReportCoveragePeriodFindResponse findCoveragePeriod(
            @AuthenticatedUserId Long userId,
            @RequestParam LocalDate reportOn,
            @RequestParam ReportType reportType
    ) {
        ReportCoveragePeriodFindQuery query = new ReportCoveragePeriodFindQuery(userId, reportOn, reportType);

        return ReportCoveragePeriodFindResponse.from(reportFinder.findCoveragePeriod(query));
    }

    @GetMapping("/writing-statuses")
    public ReportWritingStatusesFindResponse findWritingStatuses(@AuthenticatedUserId Long userId) {
        LocalDate baseDate = toKoreanDate(currentTimeProvider.now());

        return ReportWritingStatusesFindResponse.from(reportFinder.findWritingStatuses(userId, baseDate));
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
