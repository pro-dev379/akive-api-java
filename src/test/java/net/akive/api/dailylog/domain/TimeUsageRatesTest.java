package net.akive.api.dailylog.domain;

import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static net.akive.api.dailylog.domain.DailyLogType.*;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.NOW;
import static net.akive.api.dailylog.domain.fixture.TimeUsageRatesFixture.log;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;

class TimeUsageRatesTest {

    @Test
    void 일일_로그_전체_활동_시간을_조회_기간_전체_시간으로_나누어_일일_로그_타입별_시간_사용률을_집계한다() {
        User user = createReconstructedUser();
        ActivityTime activityTime = ActivityTime.of(
                LocalDateTime.of(2026, 9, 12, 4, 0),
                LocalDateTime.of(2026, 9, 12, 8, 0),
                3 * 60 * 60L,
                false,
                NOW
        );
        DailyLog studyDailyLog = DailyLog.create(user, activityTime, STUDY, "학습", NOW);

        TimeUsageRates rates = TimeUsageRates.calculate(
                LocalDate.of(2026, 9, 11),
                LocalDate.of(2026, 9, 12),
                List.of(studyDailyLog)
        );

        assertThat(rates.ratesByType().get(STUDY)).isEqualByComparingTo("8.3");
        assertThat(rates.unrecordedRate()).isEqualByComparingTo("91.7");
    }

    @Test
    void 조회_기간_경계에_일부_오버랩된_일일_로그는_오버랩된_시간만_집계한다() {
        DailyLog study = log(
                LocalDateTime.of(2026, 9, 12, 4, 0),
                LocalDateTime.of(2026, 9, 12, 8, 0),
                STUDY
        );

        TimeUsageRates rates = TimeUsageRates.calculate(
                LocalDate.of(2026, 9, 12), LocalDate.of(2026, 9, 12), List.of(study)
        );

        assertThat(rates.ratesByType().get(STUDY)).isEqualByComparingTo("12.5");
        assertThat(rates.unrecordedRate()).isEqualByComparingTo("87.5");
    }

    @Test
    void 수면_일일_로그는_귀속일과_무관하게_활동_시간을_기준으로_집계한다() {
        DailyLog sleepDailyLog = log(
                LocalDateTime.of(2026, 9, 11, 21, 0),
                LocalDateTime.of(2026, 9, 12, 3, 0),
                SLEEP
        );

        assertThat(sleepDailyLog.getLogOn()).isEqualTo(LocalDate.of(2026, 9, 12));

        TimeUsageRates rates = TimeUsageRates.calculate(
                LocalDate.of(2026, 9, 11),
                LocalDate.of(2026, 9, 11),
                List.of(sleepDailyLog)
        );

        assertThat(rates.ratesByType().get(SLEEP)).isEqualByComparingTo("25.0");
    }

    @Test
    void 회원이_직접_기록한_기타와_미기록_시간을_구분한다() {
        DailyLog etc = log(
                LocalDateTime.of(2026, 9, 12, 9, 0),
                LocalDateTime.of(2026, 9, 12, 10, 0),
                ETC
        );

        TimeUsageRates rates = TimeUsageRates.calculate(
                LocalDate.of(2026, 9, 12), LocalDate.of(2026, 9, 13), List.of(etc)
        );

        assertThat(rates.ratesByType().get(ETC)).isEqualByComparingTo("2.1");
        assertThat(rates.ratesByType().get(STUDY)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(rates.unrecordedRate()).isEqualByComparingTo("97.9");
    }

    @Test
    void 일일_로그가_하나도_없으면_하루_전체를_미기록으로_집계한다() {
        TimeUsageRates rates = TimeUsageRates.calculate(
                LocalDate.of(2026, 9, 12),
                LocalDate.of(2026, 9, 13),
                List.of()
        );

        assertThat(rates.ratesByType().values())
                .allSatisfy(rate -> assertThat(rate).isEqualByComparingTo(BigDecimal.ZERO));
        assertThat(rates.unrecordedRate()).isEqualByComparingTo("100.0");
    }
}
