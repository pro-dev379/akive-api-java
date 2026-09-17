package net.akive.api.dailylog.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogCreateRequest;
import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogUpdateRequest;
import net.akive.api.dailylog.adapter.webapi.dto.response.DailyLogCreateResponse;
import net.akive.api.dailylog.adapter.webapi.dto.response.DailyLogOnsFindResponse;
import net.akive.api.dailylog.adapter.webapi.dto.response.DailyLogTypesFindResponse;
import net.akive.api.dailylog.adapter.webapi.dto.response.DailyLogsFindResponse;
import net.akive.api.dailylog.application.provided.DailyLogCreator;
import net.akive.api.dailylog.application.provided.DailyLogFinder;
import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;
import net.akive.api.dailylog.application.provided.dto.DailyLogDeleteCommand;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.common.pagination.SortDirection.DESC;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/daily-logs")
@RequiredArgsConstructor
public class DailyLogApi {

    private final DailyLogFinder dailyLogFinder;
    private final DailyLogCreator dailyLogCreator;
    private final CurrentTimeProvider currentTimeProvider;

    @GetMapping
    public DailyLogsFindResponse findAll(@AuthenticatedUserId Long userId, @RequestParam LocalDate logOn) {
        return DailyLogsFindResponse.from(dailyLogFinder.findAll(userId, logOn));
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public DailyLogCreateResponse create(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody DailyLogCreateRequest request
    ) {
        LocalDateTime requestedAt = currentTimeProvider.now();
        DailyLogCreateCommand command = request.to(userId, requestedAt);

        return new DailyLogCreateResponse(dailyLogCreator.create(command));
    }

    @PutMapping("/{dailyLogId}")
    @ResponseStatus(NO_CONTENT)
    public void update(
            @AuthenticatedUserId Long userId,
            @PathVariable Long dailyLogId,
            @Validated @RequestBody DailyLogUpdateRequest request
    ) {
        LocalDateTime requestedAt = currentTimeProvider.now();

        dailyLogCreator.update(request.to(dailyLogId, userId, requestedAt));
    }

    @DeleteMapping("/{dailyLogId}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@AuthenticatedUserId Long userId, @PathVariable Long dailyLogId) {
        dailyLogCreator.delete(new DailyLogDeleteCommand(dailyLogId, userId));
    }

    @GetMapping("/log-ons")
    public DailyLogOnsFindResponse findRecentLogOns(
            @AuthenticatedUserId Long userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        PageResult<LocalDate> logOns =
                dailyLogFinder.findLogOns(userId, PageCondition.of(page, size, "logOn", DESC));

        return DailyLogOnsFindResponse.from(logOns);
    }

    @GetMapping("/types")
    public DailyLogTypesFindResponse findDailyLogTypes() {
        return DailyLogTypesFindResponse.from(dailyLogFinder.findDailyLogTypes());
    }
}
