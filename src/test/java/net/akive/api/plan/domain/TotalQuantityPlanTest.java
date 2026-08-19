package net.akive.api.plan.domain;

import net.akive.api.common.exception.business.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static net.akive.api.common.exception.business.BadRequestError.REQUIRED_VALUE_MISSING;
import static org.assertj.core.api.Assertions.*;

class TotalQuantityPlanTest {

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
    private static final WeeklyQuantityPlan VALID_WEEKLY_QUANTITY_PLAN = new WeeklyQuantityPlan(
            VALID_WEEKLY_QUANTITY,
            List.of(VALID_DAILY_QUANTITY)
    );

    @Test
    void 세션_총량_계획을_생성한다() {
        // when
        TotalQuantityPlan totalQuantityPlan = new TotalQuantityPlan(
                VALID_TOTAL_QUANTITY,
                List.of(VALID_WEEKLY_QUANTITY_PLAN)
        );

        // then
        assertThat(totalQuantityPlan.totalQuantity()).isEqualTo(VALID_TOTAL_QUANTITY);
        assertThat(totalQuantityPlan.weeklyQuantities()).containsExactly(VALID_WEEKLY_QUANTITY_PLAN);
    }

    @Test
    void 세션_총량_계획의_세션_총량은_null일_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new TotalQuantityPlan(
                        null,
                        List.of(VALID_WEEKLY_QUANTITY_PLAN)
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 세션_총량_계획의_주간_계획_목록은_null일_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new TotalQuantityPlan(
                        VALID_TOTAL_QUANTITY,
                        null
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 세션_총량_계획의_주간_계획_목록은_생성_후_원본_목록_변경의_영향을_받지_않는다() {
        // given
        List<WeeklyQuantityPlan> weeklyQuantityPlans = new ArrayList<>();
        weeklyQuantityPlans.add(VALID_WEEKLY_QUANTITY_PLAN);

        // when
        TotalQuantityPlan totalQuantityPlan = new TotalQuantityPlan(
                VALID_TOTAL_QUANTITY,
                weeklyQuantityPlans
        );
        weeklyQuantityPlans.clear();

        // then
        assertThat(totalQuantityPlan.weeklyQuantities()).containsExactly(VALID_WEEKLY_QUANTITY_PLAN);
    }

    @Test
    void 세션_총량_계획의_주간_계획_목록은_수정할_수_없다() {
        // given
        TotalQuantityPlan totalQuantityPlan = new TotalQuantityPlan(
                VALID_TOTAL_QUANTITY,
                List.of(VALID_WEEKLY_QUANTITY_PLAN)
        );

        // then
        assertThatThrownBy(() -> totalQuantityPlan.weeklyQuantities().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
