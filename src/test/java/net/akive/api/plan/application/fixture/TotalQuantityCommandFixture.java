package net.akive.api.plan.application.fixture;

import net.akive.api.plan.application.provided.totalquantity.dto.*;
import net.akive.api.plan.domain.value.*;

import static net.akive.api.plan.domain.fixture.DailyQuantityFixture.FIRST_DAILY_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.DailyQuantityFixture.SECOND_DAILY_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.SESSIONAL_PLAN_ID;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.FIRST_TOTAL_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.WeeklyQuantityFixture.FIRST_WEEKLY_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.WeeklyQuantityFixture.SECOND_WEEKLY_QUANTITY_ID;
import static net.akive.api.user.domain.UserFixture.USER_ID;

public class TotalQuantityCommandFixture {

    private TotalQuantityCommandFixture() {
    }

    public static TotalQuantityCreateCommand createTotalQuantityCreateCommand(
            Subject subject,
            TotalQuantityAmount totalQuantityAmount
    ) {
        return new TotalQuantityCreateCommand(
                SESSIONAL_PLAN_ID,
                USER_ID,
                subject,
                totalQuantityAmount
        );
    }

    public static TotalQuantityUpdateCommand createTotalQuantityUpdateCommand(
            Subject targetSubject,
            TotalQuantityAmount targetTotalQuantityAmount
    ) {
        return new TotalQuantityUpdateCommand(
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                targetSubject,
                targetTotalQuantityAmount
        );
    }

    public static TotalQuantityDeleteCommand createTotalQuantityDeleteCommand(Long totalQuantityId) {
        return new TotalQuantityDeleteCommand(
                SESSIONAL_PLAN_ID,
                USER_ID,
                totalQuantityId
        );
    }

    public static WeeklyQuantityAmountUpdateCommand createWeeklyQuantityAmountUpdateCommand(
            Long weeklyQuantityId,
            WeeklyQuantityAmount targetWeeklyQuantityAmount
    ) {
        return new WeeklyQuantityAmountUpdateCommand(
                weeklyQuantityId,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                targetWeeklyQuantityAmount
        );
    }

    public static WeeklyAmountDelayCommand createWeeklyAmountDelayCommand(
            Long weeklyQuantityId,
            Long receivingWeeklyQuantityId,
            int weeklyAmountToDelay
    ) {
        return WeeklyAmountDelayCommand.of(
                weeklyQuantityId,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                receivingWeeklyQuantityId,
                weeklyAmountToDelay
        );
    }

    public static WeeklyAmountDelayCommand createWeeklyAmountDelayCommand(
            Long receivingWeeklyQuantityId,
            int weeklyAmountToDelay
    ) {
        return WeeklyAmountDelayCommand.of(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                receivingWeeklyQuantityId,
                weeklyAmountToDelay
        );
    }

    public static WeeklyAmountDelayCommand createWeeklyAmountDelayCommandWithTotalQuantityId(Long totalQuantityId) {
        return WeeklyAmountDelayCommand.of(
                FIRST_WEEKLY_QUANTITY_ID,
                totalQuantityId,
                SESSIONAL_PLAN_ID,
                USER_ID,
                SECOND_WEEKLY_QUANTITY_ID,
                10
        );
    }

    public static DailyAmountDelayCommand createDailyAmountDelayCommandWithTotalQuantityId(Long totalQuantityId) {
        return DailyAmountDelayCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                totalQuantityId,
                SESSIONAL_PLAN_ID,
                USER_ID,
                SECOND_DAILY_QUANTITY_ID,
                10
        );
    }

    public static DailyAmountDelayCommand createDailyAmountDelayCommand(int dailyAmountToDelay) {
        return DailyAmountDelayCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                SECOND_DAILY_QUANTITY_ID,
                dailyAmountToDelay
        );
    }

    public static DailyAmountUpdateCommand createDailyAmountUpdateCommand(
            int targetDailyAmount,
            String targetDailyMemo
    ) {
        return createDailyAmountUpdateCommand(
                new QuantityAmount(targetDailyAmount),
                new QuantityMemo(targetDailyMemo)
        );
    }

    public static DailyAmountUpdateCommand createDailyAmountUpdateCommand(
            QuantityAmount targetDailyAmount,
            QuantityMemo targetDailyMemo
    ) {
        return new DailyAmountUpdateCommand(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                targetDailyAmount,
                targetDailyMemo
        );
    }

    public static DailyCompletedAmountUpdateCommand createDailyCompletedAmountUpdateCommand(
            int targetDailyCompletedAmount
    ) {
        return DailyCompletedAmountUpdateCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                targetDailyCompletedAmount
        );
    }

}
