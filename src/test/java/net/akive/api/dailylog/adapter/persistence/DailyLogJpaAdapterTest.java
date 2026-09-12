package net.akive.api.dailylog.adapter.persistence;

import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.LOG_ON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DailyLogJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 회원_식별자와_날짜로_일일_로그_목록을_시작_시간_오름차순으로_조회한다() {
        User savedUser = saveUser();
        DailyLog recentDailyLog = saveDailyLog(savedUser, LOG_ON, 10);
        DailyLog olderDailyLog = saveDailyLog(savedUser, LOG_ON, 9);
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
    void 회원_식별자로_일일_로그가_있는_날짜_목록을_중복_없이_페이징_조회한다() {
        User savedUser = saveUser();
        LocalDate olderLogOn = LocalDate.of(2026, 9, 1);
        LocalDate recentLogOn = olderLogOn.plusDays(1);
        saveDailyLog(savedUser, olderLogOn, 9);
        saveDailyLog(savedUser, olderLogOn, 10);
        saveDailyLog(savedUser, recentLogOn, 9);
        flushAndClear();

        PageResult<LocalDate> foundLogOns = dailyLogRepository.findLogOnsByUserId(
                savedUser.getUserId(),
                PageCondition.of(0, 1, "logOn", DESC)
        );

        assertThat(foundLogOns.page()).isZero();
        assertThat(foundLogOns.size()).isEqualTo(1);
        assertThat(foundLogOns.totalElement()).isEqualTo(2);
        assertThat(foundLogOns.totalPage()).isEqualTo(2);
        assertThat(foundLogOns.first()).isTrue();
        assertThat(foundLogOns.last()).isFalse();
        assertThat(foundLogOns.contents()).containsExactly(recentLogOn);
    }

    @Test
    void 일일_로그_날짜_목록_조회에서_지원하지_않는_정렬_속성이면_조회할_수_없다() {
        User savedUser = saveUser();

        assertThatExceptionOfType(InvalidDataAccessApiUsageException.class)
                .isThrownBy(() -> dailyLogRepository.findLogOnsByUserId(
                        savedUser.getUserId(),
                        PageCondition.of(0, 10, "unknownProperty", DESC)
                ))
                .withMessageContaining("Unsupported daily log sort property: unknownProperty")
                .withCauseInstanceOf(IllegalArgumentException.class);
    }
}
