package net.akive.api.dailylog.domain;

import net.akive.api.user.domain.GoalVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;
import static net.akive.api.common.time.ServiceDayPolicy.toServiceDayStart;
import static net.akive.api.user.domain.fixture.GoalFixture.createGoalVersion;
import static net.akive.api.user.domain.fixture.GoalFixture.goalVersionWithoutMealAndRestAllowance;
import static org.assertj.core.api.Assertions.assertThat;

class FocusMetricsTest {

    @Test
    void 일간_경계_시간을_가로지른_학습_로그의_집중_시간을_각_일자의_활동_시간에_비례해_배분한다() {
        DailyLog fourHourStudyAcrossBoundary = study(LOG_ON.plusDays(1), 4, 8, hours(3));
        GoalVersion goalVersion = createGoalVersion(toServiceDayStart(LOG_ON.minusDays(1)));

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
                toServiceDayStart(LOG_ON.minusDays(1))
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
        GoalVersion goalVersion = createGoalVersion(toServiceDayStart(LOG_ON.minusDays(1)));

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
        GoalVersion goalVersion = createGoalVersion(toServiceDayStart(LOG_ON.minusDays(1)));

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
        GoalVersion goalVersion = createGoalVersion(toServiceDayStart(LOG_ON.minusDays(1)));

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
    void 서비스일별_집중도에_해당_서비스일_오전_5시_전에_생성된_최신_목표를_적용한다() {
        LocalDate targetOn = LOG_ON.plusDays(1);
        List<DailyLog> dailyLogs = List.of(
                study(targetOn, 9, 10, hours(1)),
                meal(targetOn, 10, 11),
                study(targetOn, 11, 12, hours(1))
        );
        GoalVersion goalWithoutMealAllowance =
                goalVersionWithoutMealAndRestAllowance(toServiceDayStart(targetOn.minusDays(1)));
        GoalVersion latestGoalWithMealAllowance = createGoalVersion(toServiceDayStart(targetOn).minusSeconds(60));

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
    void 목표는_생성_후_다음_서비스일_오전_5시부터_적용한다() {
        LocalDate nextOn = LOG_ON.plusDays(1);
        LocalDate dayAfterNextOn = nextOn.plusDays(1);
        List<DailyLog> dailyLogs = List.of(
                study(nextOn, 9, 10, hours(1)),
                meal(nextOn, 10, 11),
                study(nextOn, 11, 12, hours(1)),
                study(dayAfterNextOn, 9, 10, hours(1)),
                meal(dayAfterNextOn, 10, 11),
                study(dayAfterNextOn, 11, 12, hours(1))
        );
        GoalVersion currentGoalWithoutMealAllowance = goalVersionWithoutMealAndRestAllowance(
                toServiceDayStart(LOG_ON)
        );
        GoalVersion goalCreatedBeforeBoundary = createGoalVersion(toServiceDayStart(nextOn).minusSeconds(60));
        GoalVersion goalCreatedAtBoundary = goalVersionWithoutMealAndRestAllowance(toServiceDayStart(nextOn));

        FocusMetrics beforeBoundaryGoalMetrics = FocusMetrics.calculate(
                nextOn,
                nextOn,
                dailyLogs,
                List.of(currentGoalWithoutMealAllowance, goalCreatedBeforeBoundary, goalCreatedAtBoundary)
        );
        FocusMetrics atBoundaryGoalMetrics = FocusMetrics.calculate(
                dayAfterNextOn,
                dayAfterNextOn,
                dailyLogs,
                List.of(currentGoalWithoutMealAllowance, goalCreatedBeforeBoundary, goalCreatedAtBoundary)
        );

        assertThat(beforeBoundaryGoalMetrics.focusRate())
                .as("KST 2026-09-13 04:59에 생성한 목표는 2026-09-13 서비스일부터 적용한다")
                .isEqualByComparingTo("100.0");
        assertThat(atBoundaryGoalMetrics.focusRate())
                .as("KST 2026-09-13 05:00에 생성한 목표는 2026-09-14 서비스일부터 적용한다")
                .isEqualByComparingTo("66.7");
    }

}
