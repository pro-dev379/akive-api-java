package net.akive.api.dailylog.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.cache.ResponseCache;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogCreateRequest;
import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogUpdateRequest;
import net.akive.api.dailylog.adapter.webapi.dto.response.DailyLogCreateResponse;
import net.akive.api.dailylog.adapter.webapi.dto.response.DailyLogOnPageFindResponse;
import net.akive.api.dailylog.adapter.webapi.dto.response.DailyLogTypesFindResponse;
import net.akive.api.dailylog.adapter.webapi.dto.response.DailyLogsFindResponse;
import net.akive.api.dailylog.application.provided.DailyLogCreator;
import net.akive.api.dailylog.application.provided.DailyLogFinder;
import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;
import net.akive.api.dailylog.application.provided.dto.DailyLogDeleteCommand;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.common.cache.CacheKeyProvider.dailyLogAnalyticsPattern;
import static net.akive.api.common.cache.CacheKeyProvider.dailyLogTypes;
import static net.akive.api.common.cache.ResponseCacheTtl.COMMON_CODE_LIST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/daily-logs")
@RequiredArgsConstructor
public class DailyLogApi {

    private final DailyLogFinder dailyLogFinder;
    private final DailyLogCreator dailyLogCreator;
    private final ResponseCache responseCache;

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
        DailyLogCreateCommand command = request.to(userId);

        DailyLogCreateResponse response = new DailyLogCreateResponse(dailyLogCreator.create(command));
        responseCache.evictByPattern(dailyLogAnalyticsPattern(userId));

        return response;
    }

    @PutMapping("/{dailyLogId}")
    @ResponseStatus(NO_CONTENT)
    public void update(
            @AuthenticatedUserId Long userId,
            @PathVariable Long dailyLogId,
            @Validated @RequestBody DailyLogUpdateRequest request
    ) {
        dailyLogCreator.update(request.to(dailyLogId, userId));
        responseCache.evictByPattern(dailyLogAnalyticsPattern(userId));
    }

    @DeleteMapping("/{dailyLogId}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@AuthenticatedUserId Long userId, @PathVariable Long dailyLogId) {
        dailyLogCreator.delete(new DailyLogDeleteCommand(dailyLogId, userId));
        responseCache.evictByPattern(dailyLogAnalyticsPattern(userId));
    }

    @GetMapping("/log-ons")
    public DailyLogOnPageFindResponse findRecentLogOns(
            @AuthenticatedUserId Long userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        PageResult<LocalDate> logOnPage =
                dailyLogFinder.findLogOnPage(userId, PageCondition.of(page, size, "logOn", DESC));

        return DailyLogOnPageFindResponse.from(logOnPage);
    }

    @GetMapping("/types")
    public DailyLogTypesFindResponse findDailyLogTypes() {
        String cacheKey = dailyLogTypes();

        return responseCache.get(cacheKey, DailyLogTypesFindResponse.class)
                .orElseGet(() -> {
                    DailyLogTypesFindResponse response =
                            DailyLogTypesFindResponse.from(dailyLogFinder.findDailyLogTypes());
                    responseCache.put(cacheKey, response, COMMON_CODE_LIST);

                    return response;
                });
    }
}
