package net.akive.api.dailylog.adapter.persistence;

import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.FocusTime;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;
import static net.akive.api.dailylog.domain.fixture.FocusTimeFixture.createFocusTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DailyLogJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 일일_로그를_저장한다() {
        User savedUser = saveUser();
        FocusTime focusTime = createFocusTime(
                startOn(LOG_ON, 9),
                endOn(LOG_ON, 10)
        );
        DailyLog dailyLog = createDailyLog(savedUser, focusTime);

        DailyLog savedDailyLog = dailyLogRepository.save(dailyLog);
        flushAndClear();

        DailyLog foundDailyLog = dailyLogJpaRepository.findById(savedDailyLog.getDailyLogId())
                .orElseThrow()
                .to(currentTimeProvider.now());

        assertThat(foundDailyLog.getDailyLogId()).isNotNull();
        assertThat(foundDailyLog.getUser().getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(foundDailyLog.getLogOn()).isEqualTo(LOG_ON);
        assertThat(foundDailyLog.getFocusTime()).isEqualTo(focusTime);
        assertThat(foundDailyLog.getDailyLogType()).isEqualTo(dailyLog.getDailyLogType());
        assertThat(foundDailyLog.getFocusTimeSec()).isEqualTo(dailyLog.getFocusTimeSec());
        assertThat(foundDailyLog.isFocusRateExcluded()).isEqualTo(dailyLog.isFocusRateExcluded());
        assertThat(foundDailyLog.getContents()).isEqualTo(dailyLog.getContents());
    }

    @Test
    void 회원_식별자로_일일_로그가_있는_날짜_목록을_중복_없이_페이징_조회한다() {
        User savedUser = saveUser();
        LocalDate olderLogOn = LocalDate.of(2026, 9, 1);
        LocalDate recentLogOn = olderLogOn.plusDays(1);
        saveDailyLog(savedUser, olderLogOn, 9);
        saveDailyLog(savedUser, olderLogOn, 10);
        saveDailyLog(savedUser, recentLogOn, 9);
        flushAndClear();

        PageResult<LocalDate> foundLogOnPage = dailyLogRepository.findLogOnPageByUserId(
                savedUser.getUserId(),
                PageCondition.of(0, 1, "logOn", DESC)
        );

        assertThat(foundLogOnPage.pageInfo().page()).isZero();
        assertThat(foundLogOnPage.pageInfo().size()).isEqualTo(1);
        assertThat(foundLogOnPage.pageInfo().totalElement()).isEqualTo(2);
        assertThat(foundLogOnPage.pageInfo().totalPage()).isEqualTo(2);
        assertThat(foundLogOnPage.pageInfo().first()).isTrue();
        assertThat(foundLogOnPage.pageInfo().last()).isFalse();
        assertThat(foundLogOnPage.contents()).containsExactly(recentLogOn);
    }

    @Test
    void 일일_로그_날짜_목록_조회에서_지원하지_않는_정렬_속성이면_조회할_수_없다() {
        User savedUser = saveUser();

        assertThatExceptionOfType(InvalidDataAccessApiUsageException.class)
                .isThrownBy(() -> dailyLogRepository.findLogOnPageByUserId(
                        savedUser.getUserId(),
                        PageCondition.of(0, 10, "unknownProperty", DESC)
                ))
                .withMessageContaining("Unsupported daily log sort property: unknownProperty")
                .withCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 회원_식별자와_날짜로_일일_로그_목록을_시작_시간_오름차순으로_조회한다() {
        User savedUser = saveUser();
        FocusTime recentFocusTime = createFocusTime(
                startOn(LOG_ON, 10),
                endOn(LOG_ON, 11)
        );
        FocusTime olderFocusTime = createFocusTime(
                startOn(LOG_ON, 9),
                endOn(LOG_ON, 10)
        );
        DailyLog recentDailyLog = saveDailyLog(savedUser, recentFocusTime);
        DailyLog olderDailyLog = saveDailyLog(savedUser, olderFocusTime);
        flushAndClear();

        List<DailyLog> foundDailyLogs = dailyLogRepository.findAllByUserIdAndLogOnOrderByStartOn(
                savedUser.getUserId(),
                LOG_ON
        );

        assertThat(foundDailyLogs)
                .extracting(DailyLog::getDailyLogId)
                .containsExactly(olderDailyLog.getDailyLogId(), recentDailyLog.getDailyLogId());
    }

    @Test
    void 회원_식별자와_시간_범위로_기존과_오버랩되는_일일_로그가_있는지_확인한다() {
        User savedUser = saveUser();
        FocusTime storedFocusTime = createFocusTime(
                startOn(LOG_ON, 9),
                endOn(LOG_ON, 10)
        );
        saveDailyLog(savedUser, storedFocusTime);
        flushAndClear();
        FocusTime overlappingFocusTime = createFocusTime(
                startOn(LOG_ON, 9).plusMinutes(30),
                endOn(LOG_ON, 9).plusMinutes(40)
        );

        boolean exists = dailyLogRepository.existsOverlappingTimeByUserIdAndFocusTime(
                savedUser.getUserId(),
                overlappingFocusTime
        );

        assertThat(exists).isTrue();
    }

    @Test
    void 한_일일_로그의_종료_시간이_다른_일일_로그의_시작_시간과_같아도_오버랩되지_않는댜() {
        User savedUser = saveUser();
        FocusTime storedFocusTime = createFocusTime(
                startOn(LOG_ON, 9),
                endOn(LOG_ON, 10)
        );
        saveDailyLog(savedUser, storedFocusTime);
        flushAndClear();
        FocusTime nonOverlappingFocusTime = createFocusTime(
                startOn(LOG_ON, 10),
                endOn(LOG_ON, 11)
        );

        boolean exists = dailyLogRepository.existsOverlappingTimeByUserIdAndFocusTime(
                savedUser.getUserId(),
                nonOverlappingFocusTime
        );

        assertThat(exists).isFalse();
    }

    @Test
    void 제외할_일일_로그_식별자에_대해서는_오버랩_검증에서_제외한다() {
        User savedUser = saveUser();
        FocusTime storedFocusTime = createFocusTime(
                startOn(LOG_ON, 9),
                endOn(LOG_ON, 10)
        );
        DailyLog savedDailyLog = saveDailyLog(savedUser, storedFocusTime);
        flushAndClear();

        boolean exists = dailyLogRepository.existsOverlappingTimeByUserIdAndFocusTimeAndDailyLogIdNot(
                savedUser.getUserId(),
                storedFocusTime,
                savedDailyLog.getDailyLogId()
        );

        assertThat(exists).isFalse();
    }

    @Test
    void 일일_로그를_삭제한다() {
        User savedUser = saveUser();
        DailyLog savedDailyLog = saveDailyLog(savedUser, LOG_ON, 9);
        flushAndClear();

        dailyLogRepository.delete(savedDailyLog);
        flushAndClear();

        assertThat(dailyLogJpaRepository.findById(savedDailyLog.getDailyLogId())).isEmpty();
    }
}
