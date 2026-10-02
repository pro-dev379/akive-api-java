package net.akive.api.dailylog.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.DAILY_LOG_FOCUS_TIME_NOT_ALLOWED;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.DAILY_LOG_SLEEP_TIME_TOO_SHORT;
import static net.akive.api.dailylog.domain.DailyLogType.*;
import static net.akive.api.dailylog.domain.fixture.ActivityTimeFixture.createActivityTime;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;
import static org.assertj.core.api.Assertions.*;

class DailyLogTest {

    @Test
    void 저장된_일일_로그를_복원한다() {
        User user = createUser();
        DailyLog dailyLog = DailyLog.reconstruct(
                DAILY_LOG_ID,
                user,
                LOG_ON,
                ACTIVITY_TIME,
                STUDY,
                CONTENTS,
                CURRENT_INSTANT
        );

        assertThat(dailyLog.getDailyLogId()).isEqualTo(DAILY_LOG_ID);
        assertThat(dailyLog.getUser()).isEqualTo(user);
        assertThat(dailyLog.getLogOn()).isEqualTo(LOG_ON);
        assertThat(dailyLog.getActivityTime()).isEqualTo(ACTIVITY_TIME);
        assertThat(dailyLog.getDailyLogType()).isEqualTo(STUDY);
        assertThat(dailyLog.getFocusTimeSec()).isEqualTo(FOCUS_TIME_SEC);
        assertThat(dailyLog.isFocusRateExcluded()).isFalse();
        assertThat(dailyLog.getContents()).isEqualTo(CONTENTS);
    }

    @Test
    void 저장된_수면_일일_로그가_2시간_미만이면_복원할_수_없다() {
        ActivityTime activityTime = ActivityTime.of(
                START_ON,
                START_ON.plusHours(2).minusSeconds(1),
                null,
                true,
                CURRENT_INSTANT
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> DailyLog.reconstruct(
                        DAILY_LOG_ID,
                        createUser(),
                        LOG_ON,
                        activityTime,
                        SLEEP,
                        CONTENTS,
                        CURRENT_INSTANT
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_SLEEP_TIME_TOO_SHORT);
    }

    @Test
    void 저장된_비학습_일일_로그에_집중_시간이_있으면_복원할_수_없다() {
        ActivityTime activityTime = ActivityTime.of(START_ON, START_ON.plusHours(3), 0L, true, CURRENT_INSTANT);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> DailyLog.reconstruct(
                        DAILY_LOG_ID,
                        createUser(),
                        LOG_ON,
                        activityTime,
                        ATTENDANCE,
                        CONTENTS,
                        CURRENT_INSTANT
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_FOCUS_TIME_NOT_ALLOWED);
    }

    @Test
    void 일일_로그를_생성한다() {
        User user = createUser();
        DailyLog dailyLog = DailyLog.create(
                user,
                ACTIVITY_TIME,
                STUDY,
                CONTENTS,
                CURRENT_INSTANT
        );

        assertThat(dailyLog.getUser()).isEqualTo(user);
        assertThat(dailyLog.getLogOn()).isEqualTo(LOG_ON);
        assertThat(dailyLog.getActivityTime()).isEqualTo(ACTIVITY_TIME);
        assertThat(dailyLog.getDailyLogType()).isEqualTo(STUDY);
        assertThat(dailyLog.getFocusTimeSec()).isEqualTo(FOCUS_TIME_SEC);
        assertThat(dailyLog.isFocusRateExcluded()).isFalse();
        assertThat(dailyLog.getContents()).isEqualTo(CONTENTS);
    }

    @Test
    void 시작_시간이_일간_경계_시간_이전인_일일_로그는_전날에_귀속되어_생성된다() {
        LocalDateTime startOn = LocalDateTime.of(2026, 9, 13, 4, 0);
        ActivityTime activityTime = ActivityTime.of(startOn, startOn.plusHours(4), 10_800L, false, CURRENT_INSTANT);

        DailyLog dailyLog = DailyLog.create(createUser(), activityTime, STUDY, CONTENTS, CURRENT_INSTANT);

        assertThat(dailyLog.getLogOn()).isEqualTo(LocalDate.of(2026, 9, 12));
    }

    @Test
    void 시작_시간이_일간_경계_시간인_일일_로그는_당일에_귀속되어_생성된다() {
        LocalDateTime startOn = LocalDateTime.of(2026, 9, 13, 5, 0);
        ActivityTime activityTime = ActivityTime.of(startOn, startOn.plusHours(1), null, false, CURRENT_INSTANT);

        DailyLog dailyLog = DailyLog.create(createUser(), activityTime, STUDY, CONTENTS, CURRENT_INSTANT);

        assertThat(dailyLog.getLogOn()).isEqualTo(startOn.toLocalDate());
    }

    @Test
    void 수면은_수면_종료일에_귀속되고_집중도_계산에서_제외되어_생성된다() {
        LocalDateTime startOn = LocalDateTime.of(2026, 9, 12, 21, 0);
        ActivityTime activityTime = ActivityTime.of(startOn, startOn.plusHours(6), null, false, CURRENT_INSTANT);

        DailyLog dailyLog = DailyLog.create(createUser(), activityTime, SLEEP, CONTENTS, CURRENT_INSTANT);

        assertThat(dailyLog.getLogOn()).isEqualTo(LocalDate.of(2026, 9, 13));
        assertThat(dailyLog.isFocusRateExcluded()).isTrue();
    }

    @Test
    void 수면_시간이_2시간인_일일_로그를_생성한다() {
        LocalDateTime startOn = LocalDateTime.of(2026, 9, 12, 23, 0);
        ActivityTime activityTime = ActivityTime.of(startOn, startOn.plusHours(2), null, false, CURRENT_INSTANT);

        assertThatCode(() -> DailyLog.create(createUser(), activityTime, SLEEP, CONTENTS, CURRENT_INSTANT))
                .doesNotThrowAnyException();
    }

    @Test
    void 수면_시간이_2시간_미만이면_일일_로그를_생성할_수_없다() {
        LocalDateTime startOn = LocalDateTime.of(2026, 9, 12, 21, 0);
        ActivityTime activityTime = ActivityTime.of(
                startOn,
                startOn.plusHours(2).minusSeconds(1),
                null,
                false,
                CURRENT_INSTANT
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> DailyLog.create(createUser(), activityTime, SLEEP, CONTENTS, CURRENT_INSTANT))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_SLEEP_TIME_TOO_SHORT);
    }

    @ParameterizedTest
    @EnumSource(value = DailyLogType.class, names = "STUDY", mode = EnumSource.Mode.EXCLUDE)
    void 비학습_일일_로그에_집중_시간을_입력해서_생성할_수_없다(DailyLogType dailyLogType) {
        ActivityTime activityTime = ActivityTime.of(START_ON, START_ON.plusHours(3), 0L, false, CURRENT_INSTANT);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> DailyLog.create(createUser(), activityTime, dailyLogType, CONTENTS, CURRENT_INSTANT))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_FOCUS_TIME_NOT_ALLOWED);
    }

    @Test
    void 일일_로그를_수정한다() {
        ActivityTime targetActivityTime = createActivityTime(TARGET_START_ON, TARGET_END_ON, null, true);
        DailyLog dailyLog = DailyLog.create(
                createUser(),
                ACTIVITY_TIME,
                STUDY,
                CONTENTS,
                CURRENT_INSTANT
        );

        dailyLog.update(
                targetActivityTime,
                ATTENDANCE,
                TARGET_CONTENTS,
                CURRENT_INSTANT
        );

        assertThat(dailyLog.getLogOn()).isEqualTo(TARGET_START_ON.toLocalDate());
        assertThat(dailyLog.getActivityTime()).isEqualTo(targetActivityTime);
        assertThat(dailyLog.getDailyLogType()).isEqualTo(ATTENDANCE);
        assertThat(dailyLog.getFocusTimeSec()).isNull();
        assertThat(dailyLog.isFocusRateExcluded()).isTrue();
        assertThat(dailyLog.getContents()).isEqualTo(TARGET_CONTENTS);
    }

    @Test
    void 일일_로그_타입을_수면으로_수정하면_수면_종료일에_귀속되고_집중도에서_제외된다() {
        DailyLog dailyLog = DailyLog.create(createUser(), ACTIVITY_TIME, STUDY, CONTENTS, CURRENT_INSTANT);
        LocalDateTime startOn = LocalDateTime.of(2026, 9, 12, 23, 0);
        ActivityTime sleepTime = ActivityTime.of(startOn, startOn.plusHours(3), null, false, CURRENT_INSTANT);

        dailyLog.update(sleepTime, SLEEP, TARGET_CONTENTS, CURRENT_INSTANT);

        assertThat(dailyLog.getLogOn()).isEqualTo(LocalDate.of(2026, 9, 13));
        assertThat(dailyLog.isFocusRateExcluded()).isTrue();
    }

    @Test
    void 수면_시간이_2시간_미만이면_일일_로그를_수정할_수_없다() {
        DailyLog dailyLog = DailyLog.create(createUser(), ACTIVITY_TIME, STUDY, CONTENTS, CURRENT_INSTANT);
        LocalDateTime startOn = LocalDateTime.of(2026, 9, 12, 23, 0);
        ActivityTime sleepTime = ActivityTime.of(startOn, startOn.plusHours(1), null, false, CURRENT_INSTANT);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> dailyLog.update(sleepTime, SLEEP, TARGET_CONTENTS, CURRENT_INSTANT))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_SLEEP_TIME_TOO_SHORT);

        assertThat(dailyLog.getDailyLogType()).isEqualTo(STUDY);
        assertThat(dailyLog.getLogOn()).isEqualTo(LOG_ON);
    }

    @ParameterizedTest
    @EnumSource(value = DailyLogType.class, names = "STUDY", mode = EnumSource.Mode.EXCLUDE)
    void 비학습_일일_로그에_집중_시간을_입력해서_수정할_수_없다(DailyLogType dailyLogType) {
        DailyLog dailyLog = DailyLog.create(createUser(), ACTIVITY_TIME, STUDY, CONTENTS, CURRENT_INSTANT);
        ActivityTime activityTime = ActivityTime.of(START_ON, START_ON.plusHours(3), 0L, false, CURRENT_INSTANT);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> dailyLog.update(activityTime, dailyLogType, TARGET_CONTENTS, CURRENT_INSTANT))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_FOCUS_TIME_NOT_ALLOWED);

        assertThat(dailyLog.getDailyLogType()).isEqualTo(STUDY);
        assertThat(dailyLog.getActivityTime()).isEqualTo(ACTIVITY_TIME);
    }

    @Test
    void 같은_식별자를_가진_일일_로그는_같게_취급한다() {
        User user = createUser();
        ActivityTime targetActivityTime = createActivityTime(TARGET_START_ON, TARGET_END_ON, null, true);
        DailyLog dailyLog = DailyLog.reconstruct(
                DAILY_LOG_ID,
                user,
                LOG_ON,
                ACTIVITY_TIME,
                STUDY,
                CONTENTS,
                CURRENT_INSTANT
        );
        DailyLog sameDailyLog = DailyLog.reconstruct(
                DAILY_LOG_ID,
                user,
                TARGET_LOG_ON,
                targetActivityTime,
                ATTENDANCE,
                TARGET_CONTENTS,
                CURRENT_INSTANT
        );

        assertThat(dailyLog).isEqualTo(sameDailyLog);
        assertThat(dailyLog.hashCode()).isEqualTo(sameDailyLog.hashCode());
    }

    @Test
    void 식별자가_다른_일일_로그는_다르게_취급한다() {
        User user = createUser();
        DailyLog dailyLog = DailyLog.reconstruct(
                DAILY_LOG_ID,
                user,
                LOG_ON,
                ACTIVITY_TIME,
                STUDY,
                CONTENTS,
                CURRENT_INSTANT
        );
        DailyLog anotherDailyLog = DailyLog.reconstruct(
                ANOTHER_DAILY_LOG_ID,
                user,
                LOG_ON,
                ACTIVITY_TIME,
                STUDY,
                CONTENTS,
                CURRENT_INSTANT
        );

        assertThat(dailyLog).isNotEqualTo(anotherDailyLog);
    }

    @Test
    void 식별자가_없는_일일_로그끼리는_나머지_값이_같더라도_다른_것으로_취급한다() {
        User user = createUser();
        DailyLog dailyLog = DailyLog.create(user, ACTIVITY_TIME, STUDY, CONTENTS, CURRENT_INSTANT);
        DailyLog anotherDailyLog = DailyLog.create(user, ACTIVITY_TIME, STUDY, CONTENTS, CURRENT_INSTANT);

        assertThat(dailyLog).isNotEqualTo(anotherDailyLog);
    }
}
