package net.akive.api.dailylog.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.dailylog.adapter.webapi.dto.response.DailyLogsFindResponse;
import net.akive.api.dailylog.application.provided.DailyLogFinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static net.akive.api.common.pagination.SortDirection.DESC;

@RestController
@RequestMapping("/daily-logs")
@RequiredArgsConstructor
public class DailyLogApi {

    private final DailyLogFinder dailyLogFinder;

    @GetMapping("/{logOn}")
    public DailyLogsFindResponse findAll(@AuthenticatedUserId Long userId, @PathVariable LocalDate logOn) {
        return DailyLogsFindResponse.from(dailyLogFinder.findAll(userId, logOn));
    }

    @GetMapping("/log-ons")
    public PageResult<LocalDate> findRecentLogOns(
            @AuthenticatedUserId Long userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return dailyLogFinder.findLogOns(userId, PageCondition.of(page, size, "logOn", DESC));
    }
}
