package net.akive.api.dailylog.domain;

import net.akive.api.user.domain.GoalVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;
import static net.akive.api.user.domain.fixture.GoalFixture.createGoalVersion;
import static net.akive.api.user.domain.fixture.GoalFixture.goalVersionWithoutBreakAllowance;
import static org.assertj.core.api.Assertions.assertThat;

class FocusMetricsTest {

    @Test
    void 일간_경계_시간을_가로지른_학습_로그의_집중_시간을_각_일자의_활동_시간에_비례해_배분한다() {
        DailyLog fourHourStudyAcrossBoundary = study(LOG_ON.plusDays(1), 4, 8, hours(3));
        GoalVersion goalVersion = createGoalVersion(LOG_ON.minusDays(1).atStartOfDay());

        FocusMetrics previousDayMetrics = FocusMetrics.calculate(
                LOG_ON,
                LOG_ON,
                List.of(fourHourStudyAcrossBoundary),
                List.of(goalVersion)
        );
        FocusMetrics nextDayMetrics = FocusMetrics.calculate(
                LOG_ON.plusDays(1),
                LOG_ON.plusDays(1),
                List.of(fourHourStudyAcrossBoundary),
                List.of(goalVersion)
        );

        assertThat(previousDayMetrics.focusTimeSec())
                .as("04:00~05:00 1시간에는 전체 집중 시간 3시간의 1/4을 배분한다")
                .isEqualTo(minutes(45));
        assertThat(nextDayMetrics.focusTimeSec())
                .as("05:00~08:00 3시간에는 전체 집중 시간 3시간의 3/4을 배분한다")
                .isEqualTo(hours(2) + minutes(15));
        assertThat(previousDayMetrics.focusTimeSec() + nextDayMetrics.focusTimeSec())
                .as("경계 양쪽에 배분한 집중 시간의 합은 원래 집중 시간과 같다")
                .isEqualTo(hours(3));
    }

    @Test
    void 식사와_휴식은_목표에서_허용한_시간만_집중도_계산에서_제외한다() {
        List<DailyLog> dailyLogs = List.of(
                study(LOG_ON, 9, 10, hours(1)),
                meal(LOG_ON, 10, 11),
                rest(LOG_ON, 11, 12),
                study(LOG_ON, 12, 13, hours(1))
        );
        GoalVersion thirtyMinuteMealAndRestGoal = createGoalVersion(
                150,
                1,
                minutes(30),
                LOG_ON.atStartOfDay()
        );

        FocusMetrics metrics = FocusMetrics.calculate(
                LOG_ON,
                LOG_ON,
                dailyLogs,
                List.of(thirtyMinuteMealAndRestGoal)
        );

        assertThat(metrics.focusTimeSec()).isEqualTo(hours(2));
        assertThat(metrics.focusRate())
                .as("4시간 중 식사 30분과 휴식 30분만 제외하면 3시간이 계산 대상이다")
                .isEqualByComparingTo("66.7");
    }

    @Test
    void 수면과_집중도_계산_제외_설정한_기타_일일_로그는_집중도_계산에서_빼고_미기록_시간은_집중도_계산에_포함한다() {
        List<DailyLog> dailyLogs = List.of(
                study(LOG_ON, 9, 10, hours(1)),
                sleep(LOG_ON, 10, 12),
                etc(LOG_ON, 12, 13, true),
                study(LOG_ON, 14, 15, hours(1))
        );
        GoalVersion goalVersion = createGoalVersion(LOG_ON.minusDays(1).atStartOfDay());

        FocusMetrics metrics = FocusMetrics.calculate(LOG_ON, LOG_ON, dailyLogs, List.of(goalVersion));

        assertThat(metrics.focusTimeSec()).isEqualTo(hours(2));
        assertThat(metrics.focusRate())
                .as("6시간 중 수면 2시간과 집중도 계산 제외 설정한 1시간을 빼되, 미기록 1시간은 집중도 계산에 포함한다")
                .isEqualByComparingTo("66.7");
    }

    @Test
    void 집중도_계산_제외_설정하지_않은_기타_일일_로그는_집중도_계산에_포함한다() {
        List<DailyLog> dailyLogs = List.of(
                study(LOG_ON, 9, 10, hours(1)),
                etc(LOG_ON, 10, 11, false),
                study(LOG_ON, 11, 12, hours(1))
        );
        GoalVersion goalVersion = createGoalVersion(LOG_ON.minusDays(1).atStartOfDay());

        FocusMetrics metrics = FocusMetrics.calculate(LOG_ON, LOG_ON, dailyLogs, List.of(goalVersion));

        assertThat(metrics.focusTimeSec()).isEqualTo(hours(2));
        assertThat(metrics.focusRate())
                .as("집중도 계산 제외 설정하지 않은 1시간을 포함한 3시간 중 2시간 집중한 것으로 계산한다")
                .isEqualByComparingTo("66.7");
    }

    @Test
    void 기간_집중도는_일별_집중도의_평균이_아니라_기간_전체_시간을_합산해_계산한다() {
        LocalDate nextOn = LOG_ON.plusDays(1);
        DailyLog firstDayStudy = study(LOG_ON, 9, 11, hours(1));
        DailyLog secondDayStudy = study(nextOn, 9, 17, hours(6));
        GoalVersion goalVersion = createGoalVersion(LOG_ON.minusDays(1).atStartOfDay());

        FocusMetrics metrics = FocusMetrics.calculate(
                LOG_ON,
                nextOn,
                List.of(firstDayStudy, secondDayStudy),
                List.of(goalVersion)
        );

        assertThat(metrics.focusTimeSec()).isEqualTo(hours(7));
        assertThat(metrics.focusRate())
                .as("일별 집중도 50%와 75%의 평균인 62.5%가 아니라, 전체 10시간 중 집중한 7시간의 비율로 계산한다")
                .isEqualByComparingTo("70.0");
    }

    @Test
    void 각_날짜의_집중_시간과_집중도를_계산할_때_각_날짜_기준의_가장_최근_목표를_적용한다() {
        LocalDate targetOn = LOG_ON.plusDays(1);
        List<DailyLog> dailyLogs = List.of(
                study(targetOn, 9, 10, hours(1)),
                meal(targetOn, 10, 11),
                study(targetOn, 11, 12, hours(1))
        );
        GoalVersion goalWithoutMealAllowance = goalVersionWithoutBreakAllowance(targetOn.minusDays(2).atStartOfDay());
        GoalVersion latestGoalWithMealAllowance = createGoalVersion(targetOn.minusDays(1).atTime(20, 0));

        FocusMetrics metrics = FocusMetrics.calculate(
                targetOn,
                targetOn,
                dailyLogs,
                List.of(goalWithoutMealAllowance, latestGoalWithMealAllowance)
        );

        assertThat(metrics.focusRate())
                .as("가장 최근 목표가 식사 1시간을 허용하므로 전체 학습 시간 3시간 중 이를 뺀 2시간에 대해서만 계산한다")
                .isEqualByComparingTo("100.0");
    }

    @Test
    void 목표의_UTC_생성_시각을_한국_시각으로_변환해_목표_적용일을_결정한다() {
        LocalDate nextOn = LOG_ON.plusDays(1);
        List<DailyLog> dailyLogs = List.of(
                study(LOG_ON, 9, 10, hours(1)),
                meal(LOG_ON, 10, 11),
                study(LOG_ON, 11, 12, hours(1)),
                study(nextOn, 9, 10, hours(1)),
                meal(nextOn, 10, 11),
                study(nextOn, 11, 12, hours(1))
        );
        GoalVersion currentGoalWithoutMealAllowance = goalVersionWithoutBreakAllowance(
                LOG_ON.minusDays(1).atStartOfDay()
        );
        GoalVersion goalCreatedAtNextDayInKorea = createGoalVersion(LOG_ON.atTime(16, 0));

        FocusMetrics beforeGoalEffectiveDate = FocusMetrics.calculate(
                LOG_ON,
                LOG_ON,
                dailyLogs,
                List.of(currentGoalWithoutMealAllowance, goalCreatedAtNextDayInKorea)
        );
        FocusMetrics onGoalEffectiveDate = FocusMetrics.calculate(
                nextOn,
                nextOn,
                dailyLogs,
                List.of(currentGoalWithoutMealAllowance, goalCreatedAtNextDayInKorea)
        );

        assertThat(beforeGoalEffectiveDate.focusRate())
                .as("UTC 16시는 한국 시각으로 다음 날이므로 기존 목표를 적용한다")
                .isEqualByComparingTo("66.7");
        assertThat(onGoalEffectiveDate.focusRate())
                .as("한국 날짜가 바뀐 뒤에는 새 목표의 식사 허용 시간을 적용한다")
                .isEqualByComparingTo("100.0");
    }

}
