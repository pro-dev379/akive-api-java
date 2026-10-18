package net.akive.api.dailylog.application.service;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.application.provided.dto.FocusMetricsFindQuery;
import net.akive.api.dailylog.application.provided.dto.FocusMetricsSeriesFindQuery;
import net.akive.api.dailylog.application.provided.dto.TimeUsageRatesFindQuery;
import net.akive.api.dailylog.application.required.DailyLogRepository;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.FocusMetrics;
import net.akive.api.dailylog.domain.FocusMetricsSeries;
import net.akive.api.dailylog.domain.TimeUsageRates;
import net.akive.api.user.application.provided.GoalFinder;
import net.akive.api.user.application.provided.dto.GoalVersionsFindQuery;
import net.akive.api.user.domain.GoalVersion;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_DATE_RANGE;
import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.common.pagination.fixture.PaginationFixture.*;
import static net.akive.api.common.time.ServiceDayPolicy.DAILY_BOUNDARY_HOUR;
import static net.akive.api.common.time.ServiceDayPolicy.toServiceDayStart;
import static net.akive.api.common.analytics.TimeSeriesUnit.DAY;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;
import static net.akive.api.user.domain.fixture.GoalFixture.goalVersionWithoutMealAndRestAllowance;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DailyLogQueryServiceTest {

    @Mock
    DailyLogRepository dailyLogRepository;

    @Mock
    GoalFinder goalFinder;

    @InjectMocks
    DailyLogQueryService dailyLogQueryService;

    @Test
    void 회원_식별자로_일일_로그가_있는_날짜_목록을_페이징_조회한다() {
        List<LocalDate> contents = List.of(LOG_ON, ANOTHER_LOG_ON);
        PageCondition pageCondition = PageCondition.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, "logOn", DESC);
        PageResult<LocalDate> logOnPage = createPageResult(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, contents);

        given(dailyLogRepository.findLogOnPageByUserId(USER_ID, pageCondition)).willReturn(logOnPage);

        PageResult<LocalDate> foundLogOnPage = dailyLogQueryService.findLogOnPage(USER_ID, pageCondition);

        assertThat(foundLogOnPage).isEqualTo(logOnPage);

        then(dailyLogRepository).should().findLogOnPageByUserId(USER_ID, pageCondition);
    }

    @Test
    void 회원_식별자와_날짜로_일일_로그_목록을_조회한다() {
        User user = createReconstructedUser(USER_ID);
        List<DailyLog> dailyLogs = List.of(
                createDailyLog(user, LOG_ON, 9),
                createDailyLog(user, LOG_ON, 10)
        );

        given(dailyLogRepository.findAllByUserIdAndLogOnOrderByStartOn(USER_ID, LOG_ON))
                .willReturn(dailyLogs);

        List<DailyLog> foundDailyLogs = dailyLogQueryService.findAll(USER_ID, LOG_ON);

        assertThat(foundDailyLogs).isEqualTo(dailyLogs);

        then(dailyLogRepository).should().findAllByUserIdAndLogOnOrderByStartOn(USER_ID, LOG_ON);
    }

    @Test
    void 조회_기간_전체의_일일_로그_타입별_시간_사용률을_조회한다() {
        LocalDate endOn = LOG_ON.plusDays(1);
        givenDailyLogsForPeriod(LOG_ON, endOn);

        TimeUsageRates rates =
                dailyLogQueryService.findTimeUsageRates(new TimeUsageRatesFindQuery(USER_ID, LOG_ON, endOn));

        assertThat(rates.unrecordedRate()).isEqualByComparingTo("100.0");
        thenDailyLogsForPeriodShouldBeFound(LOG_ON, endOn);
    }

    @Test
    void 조회_기간의_일일_로그와_목표_목록으로_집중도와_집중_시간을_조회한다() {
        DailyLog dailyLog = study(LOG_ON, 9, 10, hours(1));
        GoalVersion goal = goalVersionWithoutMealAndRestAllowance(toServiceDayStart(LOG_ON.minusDays(1)));
        FocusMetricsFindQuery query = new FocusMetricsFindQuery(USER_ID, LOG_ON, LOG_ON);
        givenDailyLogsForPeriod(LOG_ON, LOG_ON, dailyLog);
        givenGoalVersionsForPeriod(LOG_ON, LOG_ON, goal);

        FocusMetrics metrics = dailyLogQueryService.findFocusMetrics(query);

        assertThat(metrics.focusTimeSec()).isEqualTo(dailyLog.getFocusTimeSec());
        thenDailyLogsForPeriodShouldBeFound(LOG_ON, LOG_ON);
        thenGoalVersionsForPeriodShouldBeFound(LOG_ON, LOG_ON);
    }

    @Test
    void 조회_기간의_일일_로그와_목표_목록으로_날짜별_집중도와_집중_시간을_계산한다() {
        LocalDate endOn = LOG_ON.plusDays(1);
        DailyLog dailyLog = study(LOG_ON, 9, 10, hours(1));
        GoalVersion goalVersion = goalVersionWithoutMealAndRestAllowance(toServiceDayStart(LOG_ON.minusDays(1)));
        FocusMetricsSeriesFindQuery query = new FocusMetricsSeriesFindQuery(USER_ID, LOG_ON, endOn, DAY);
        givenDailyLogsForPeriod(LOG_ON, endOn, dailyLog);
        givenGoalVersionsForPeriod(LOG_ON, endOn, goalVersion);

        FocusMetricsSeries series = dailyLogQueryService.findFocusMetricsSeries(query);

        assertThat(series.periodFocusMetrics()).hasSize(2);
        assertThat(series.periodFocusMetrics().getFirst().focusMetrics().focusTimeSec()).isEqualTo(hours(1));
        thenDailyLogsForPeriodShouldBeFound(LOG_ON, endOn);
        thenGoalVersionsForPeriodShouldBeFound(LOG_ON, endOn);
    }

    @Test
    void 조회_시작일이_종료일보다_늦으면_예외가_발생한다() {
        assertThatThrownBy(() -> new TimeUsageRatesFindQuery(USER_ID, LOG_ON.plusDays(1), LOG_ON))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage(INVALID_DATE_RANGE.getMessage());

        then(dailyLogRepository).shouldHaveNoInteractions();
    }

    private void givenDailyLogsForPeriod(LocalDate startOn, LocalDate endOn, DailyLog... dailyLogs) {
        given(dailyLogRepository.findAllByUserIdAndOverlappingTimeRangeOrderByStartOnAsc(
                USER_ID,
                startOn.atTime(DAILY_BOUNDARY_HOUR, 0),
                endOn.plusDays(1).atTime(DAILY_BOUNDARY_HOUR, 0)
        )).willReturn(List.of(dailyLogs));
    }

    private void givenGoalVersionsForPeriod(LocalDate startOn, LocalDate endOn, GoalVersion... goalVersions) {
        given(goalFinder.findVersionsForPeriod(new GoalVersionsFindQuery(USER_ID, startOn, endOn)))
                .willReturn(List.of(goalVersions));
    }

    private void thenDailyLogsForPeriodShouldBeFound(LocalDate startOn, LocalDate endOn) {
        then(dailyLogRepository).should().findAllByUserIdAndOverlappingTimeRangeOrderByStartOnAsc(
                USER_ID,
                startOn.atTime(DAILY_BOUNDARY_HOUR, 0),
                endOn.plusDays(1).atTime(DAILY_BOUNDARY_HOUR, 0)
        );
    }

    private void thenGoalVersionsForPeriodShouldBeFound(LocalDate startOn, LocalDate endOn) {
        then(goalFinder).should().findVersionsForPeriod(new GoalVersionsFindQuery(USER_ID, startOn, endOn));
    }
}
