package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeeklyQuantityPlanTest {

    private static final LocalDate VALID_WEEK_START_ON = LocalDate.of(2026, 7, 7);
    private static final LocalDate VALID_WEEK_END_ON = LocalDate.of(2026, 7, 14);
    private static final SessionalPlan VALID_SESSIONAL_PLAN = SessionalPlan.create(
            2L,
            1L,
            "세션",
            new SessionPeriod(VALID_WEEK_START_ON, VALID_WEEK_END_ON)
    );
    private static final TotalQuantity VALID_TOTAL_QUANTITY = TotalQuantity.create(
            2L,
            VALID_SESSIONAL_PLAN,
            new Subject("수학", "과목"),
            TotalQuantityAmount.of(100, "문제", null)
    );
    private static final WeeklyQuantity VALID_WEEKLY_QUANTITY = WeeklyQuantity.create(
            2L,
            VALID_TOTAL_QUANTITY,
            new WeekPeriod(VALID_WEEK_START_ON, VALID_WEEK_END_ON),
            WeeklyQuantityAmount.of(10, null)
    );
    private static final DailyQuantity VALID_DAILY_QUANTITY = DailyQuantity.create(
            1L,
            VALID_WEEKLY_QUANTITY,
            VALID_WEEK_START_ON,
            DailyQuantityAmount.of(10, 6, 4, "메모")
    );

    @Test
    void 주간_계획을_생성한다() {
        // when
        WeeklyQuantityPlan weeklyQuantityPlan = new WeeklyQuantityPlan(
                VALID_WEEKLY_QUANTITY,
                List.of(VALID_DAILY_QUANTITY)
        );

        // then
        assertThat(weeklyQuantityPlan.weeklyQuantity()).isEqualTo(VALID_WEEKLY_QUANTITY);
        assertThat(weeklyQuantityPlan.dailyQuantities()).containsExactly(VALID_DAILY_QUANTITY);
    }

    @Test
    void 주간_계획의_주간_계획량은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new WeeklyQuantityPlan(
                        null,
                        List.of(VALID_DAILY_QUANTITY)
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 주간_계획의_하루_계획량_목록은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new WeeklyQuantityPlan(
                        VALID_WEEKLY_QUANTITY,
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 주간_계획의_하루_계획량_목록은_생성_후_원본_목록_변경의_영향을_받지_않는다() {
        // given
        List<DailyQuantity> dailyQuantities = new ArrayList<>();
        dailyQuantities.add(VALID_DAILY_QUANTITY);

        // when
        WeeklyQuantityPlan weeklyQuantityPlan = new WeeklyQuantityPlan(
                VALID_WEEKLY_QUANTITY,
                dailyQuantities
        );
        dailyQuantities.clear();

        // then
        assertThat(weeklyQuantityPlan.dailyQuantities()).containsExactly(VALID_DAILY_QUANTITY);
    }

    @Test
    void 주간_계획의_하루_계획량_목록은_수정할_수_없다() {
        // given
        WeeklyQuantityPlan weeklyQuantityPlan = new WeeklyQuantityPlan(
                VALID_WEEKLY_QUANTITY,
                List.of(VALID_DAILY_QUANTITY)
        );

        // then
        assertThatThrownBy(() -> weeklyQuantityPlan.dailyQuantities().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}