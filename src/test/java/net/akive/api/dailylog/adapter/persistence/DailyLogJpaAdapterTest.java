package net.akive.api.dailylog.adapter.persistence;

import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.adapter.persistence.entity.DailyLogEntity;
import net.akive.api.dailylog.domain.ActivityTime;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.dailylog.domain.fixture.ActivityTimeFixture.createActivityTime;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DailyLogJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 일일_로그를_저장한다() {
        User savedUser = saveUser();
        ActivityTime activityTime = createActivityTime(
                startOn(LOG_ON, 9),
                endOn(LOG_ON, 10)
        );
        DailyLog dailyLog = createDailyLog(savedUser, activityTime);

        DailyLog savedDailyLog = dailyLogRepository.save(dailyLog);
        flushAndClear();

        DailyLog foundDailyLog = entityManager.find(DailyLogEntity.class, savedDailyLog.getDailyLogId())
                .to(currentTimeProvider.now());

        assertThat(foundDailyLog.getDailyLogId()).isNotNull();
        assertThat(foundDailyLog.getUser().getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(foundDailyLog.getLogOn()).isEqualTo(LOG_ON);
        assertThat(foundDailyLog.getActivityTime()).isEqualTo(activityTime);
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
        ActivityTime recentActivityTime = createActivityTime(
                startOn(LOG_ON, 10),
                endOn(LOG_ON, 11)
        );
        ActivityTime olderActivityTime = createActivityTime(
                startOn(LOG_ON, 9),
                endOn(LOG_ON, 10)
        );
        DailyLog recentDailyLog = saveDailyLog(savedUser, recentActivityTime);
        DailyLog olderDailyLog = saveDailyLog(savedUser, olderActivityTime);
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
    void 활동_시간이_조회_시간과_오버랩되는_일일_로그를_시작_시간_오름차순으로_조회한다() {
        User savedUser = saveUser();
        DailyLog firstMatchingLog = saveLog(savedUser, LOG_ON, 4, 6);
        DailyLog secondMatchingLog = saveLog(savedUser, LOG_ON.plusDays(1), 4, 6);
        saveLog(savedUser, LOG_ON, 3, 4);
        saveLog(savedUser, LOG_ON.plusDays(1), 6, 7);
        flushAndClear();

        List<DailyLog> foundDailyLogs = dailyLogRepository.findAllByUserIdAndOverlappingTimeRangeOrderByStartOnAsc(
                savedUser.getUserId(),
                startOn(LOG_ON, 5),
                endOn(LOG_ON.plusDays(1), 5)
        );

        assertThat(foundDailyLogs).extracting(DailyLog::getDailyLogId)
                .containsExactly(firstMatchingLog.getDailyLogId(), secondMatchingLog.getDailyLogId());
    }

    @Test
    void 회원_식별자와_시간_범위로_기존과_오버랩되는_일일_로그가_있는지_확인한다() {
        User savedUser = saveUser();
        ActivityTime storedActivityTime = createActivityTime(
                startOn(LOG_ON, 9),
                endOn(LOG_ON, 10)
        );
        saveDailyLog(savedUser, storedActivityTime);
        flushAndClear();
        ActivityTime overlappingActivityTime = createActivityTime(
                startOn(LOG_ON, 9).plusMinutes(30),
                endOn(LOG_ON, 9).plusMinutes(40)
        );

        boolean exists = dailyLogRepository.existsOverlappingTimeByUserIdAndActivityTime(
                savedUser.getUserId(),
                overlappingActivityTime
        );

        assertThat(exists).isTrue();
    }

    @Test
    void 한_일일_로그의_종료_시간이_다른_일일_로그의_시작_시간과_같아도_오버랩되지_않는댜() {
        User savedUser = saveUser();
        ActivityTime storedActivityTime = createActivityTime(
                startOn(LOG_ON, 9),
                endOn(LOG_ON, 10)
        );
        saveDailyLog(savedUser, storedActivityTime);
        flushAndClear();
        ActivityTime nonOverlappingActivityTime = createActivityTime(
                startOn(LOG_ON, 10),
                endOn(LOG_ON, 11)
        );

        boolean exists = dailyLogRepository.existsOverlappingTimeByUserIdAndActivityTime(
                savedUser.getUserId(),
                nonOverlappingActivityTime
        );

        assertThat(exists).isFalse();
    }

    @Test
    void 제외할_일일_로그_식별자에_대해서는_오버랩_검증에서_제외한다() {
        User savedUser = saveUser();
        ActivityTime storedActivityTime = createActivityTime(
                startOn(LOG_ON, 9),
                endOn(LOG_ON, 10)
        );
        DailyLog savedDailyLog = saveDailyLog(savedUser, storedActivityTime);
        flushAndClear();

        boolean exists = dailyLogRepository.existsOverlappingTimeByUserIdAndActivityTimeAndDailyLogIdNot(
                savedUser.getUserId(),
                storedActivityTime,
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

        assertThat(entityManager.find(DailyLogEntity.class, savedDailyLog.getDailyLogId())).isNull();
    }

    private DailyLog saveLog(User user, LocalDate date, int startHour, int endHour) {
        return saveDailyLog(user, createActivityTime(startOn(date, startHour), endOn(date, endHour)));
    }
}
