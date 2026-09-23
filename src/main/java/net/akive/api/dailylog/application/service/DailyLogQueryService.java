package net.akive.api.dailylog.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.application.provided.DailyLogFinder;
import net.akive.api.dailylog.application.provided.dto.FocusMetricsFindQuery;
import net.akive.api.dailylog.application.provided.dto.FocusMetricsSeriesFindQuery;
import net.akive.api.dailylog.application.provided.dto.TimeUsageRatesFindQuery;
import net.akive.api.dailylog.application.required.DailyLogRepository;
import net.akive.api.dailylog.domain.*;
import net.akive.api.user.application.provided.GoalFinder;
import net.akive.api.user.application.provided.dto.GoalVersionsFindQuery;
import net.akive.api.user.domain.GoalVersion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static net.akive.api.dailylog.domain.DailyLogPolicy.DAILY_BOUNDARY_HOUR;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class DailyLogQueryService implements DailyLogFinder {

    private final DailyLogRepository dailyLogRepository;
    private final GoalFinder goalFinder;

    @Override
    public List<DailyLog> findAll(Long userId, LocalDate logOn) {
        return dailyLogRepository.findAllByUserIdAndLogOnOrderByStartOn(userId, logOn);
    }

    @Override
    public PageResult<LocalDate> findLogOnPage(Long userId, PageCondition pageCondition) {
        return dailyLogRepository.findLogOnPageByUserId(userId, pageCondition);
    }

    @Override
    public List<DailyLogType> findDailyLogTypes() {
        return Arrays.stream(DailyLogType.values()).toList();
    }

    @Override
    public TimeUsageRates findTimeUsageRates(TimeUsageRatesFindQuery query) {
        List<DailyLog> dailyLogs = findDailyLogsOverlappingDailyBoundaryPeriod(
                query.userId(),
                query.startOn(),
                query.endOn()
        );

        return TimeUsageRates.calculate(query.startOn(), query.endOn(), dailyLogs);
    }

    @Override
    public FocusMetrics findFocusMetrics(FocusMetricsFindQuery query) {
        List<DailyLog> dailyLogs = findDailyLogsOverlappingDailyBoundaryPeriod(
                query.userId(),
                query.startOn(),
                query.endOn()
        );
        List<GoalVersion> goalVersions = findGoalVersionsForPeriod(query.userId(), query.startOn(), query.endOn());

        return FocusMetrics.calculate(query.startOn(), query.endOn(), dailyLogs, goalVersions);
    }

    @Override
    public FocusMetricsSeries findFocusMetricsSeries(FocusMetricsSeriesFindQuery query) {
        List<DailyLog> dailyLogs = findDailyLogsOverlappingDailyBoundaryPeriod(
                query.userId(),
                query.startOn(),
                query.endOn()
        );
        List<GoalVersion> goalVersions = findGoalVersionsForPeriod(query.userId(), query.startOn(), query.endOn());

        return FocusMetricsSeries.calculate(
                query.startOn(),
                query.endOn(),
                query.unit(),
                dailyLogs,
                goalVersions
        );
    }

    private List<DailyLog> findDailyLogsOverlappingDailyBoundaryPeriod(
            Long userId,
            LocalDate startOn,
            LocalDate endOn
    ) {
        return dailyLogRepository.findAllByUserIdAndOverlappingTimeRangeOrderByStartOnAsc(
                userId,
                startOn.atTime(DAILY_BOUNDARY_HOUR, 0),
                endOn.plusDays(1).atTime(DAILY_BOUNDARY_HOUR, 0)
        );
    }

    private List<GoalVersion> findGoalVersionsForPeriod(Long userId, LocalDate startOn, LocalDate endOn) {
        return goalFinder.findVersionsForPeriod(new GoalVersionsFindQuery(userId, startOn, endOn));
    }
}
