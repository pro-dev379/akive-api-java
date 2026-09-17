package net.akive.api.dailylog.application.service;

import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.application.required.DailyLogRepository;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.common.pagination.fixture.PaginationFixture.*;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DailyLogQueryServiceTest {

    @Mock
    DailyLogRepository dailyLogRepository;

    @InjectMocks
    DailyLogQueryService dailyLogQueryService;

    @Test
    void 회원_식별자로_일일_로그가_있는_날짜_목록을_페이징_조회한다() {
        List<LocalDate> contents = List.of(LOG_ON, ANOTHER_LOG_ON);
        PageCondition pageCondition = PageCondition.of(DEFAULT_PAGE, DEFAULT_SIZE, "logOn", DESC);
        PageResult<LocalDate> logOnPage = createPageResult(DEFAULT_PAGE, DEFAULT_SIZE, contents);

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
}
