package net.akive.api.plan.application.provided.dto.result.weeklyplan;

import net.akive.api.plan.application.provided.dto.result.common.DailyQuantityAmountResult;
import net.akive.api.plan.application.provided.dto.result.common.SessionWeekResult;
import net.akive.api.plan.application.provided.dto.result.common.SubjectResult;
import net.akive.api.plan.application.required.dto.WeeklyPlanDailyQuantityData;
import net.akive.api.plan.application.required.dto.WeeklyPlanWeeklyQuantityData;
import net.akive.api.plan.domain.PlanDate;
import net.akive.api.plan.domain.WeeklyQuantityAmount;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record GetWeeklyPlanResult(
        SessionWeekResult sessionWeek,
        List<PlanDateResult> planDates,
        List<WeeklyQuantityResult> weeklyQuantities
) {

    public record PlanDateResult(LocalDate date, DayOfWeek dayOfWeek) {

        public static PlanDateResult from(PlanDate planDate) {
            return new PlanDateResult(planDate.date(), planDate.dayOfWeek());
        }
    }

    public record WeeklyQuantityResult(
            Long sessionalPlanId,
            Long totalQuantityId,
            Long weeklyQuantityId,
            SubjectResult subject,
            WeeklyQuantityAmountResult weeklyQuantityAmount,
            List<DailyQuantityResult> dailyQuantities
    ) {

        public static WeeklyQuantityResult from(WeeklyPlanWeeklyQuantityData data) {
            return new WeeklyQuantityResult(
                    data.sessionalPlanId(),
                    data.totalQuantityId(),
                    data.weeklyQuantityId(),
                    SubjectResult.from(data.subject()),
                    WeeklyQuantityAmountResult.of(data.weeklyQuantityAmount(), data.unit()),
                    data.dailyQuantities().stream().map(DailyQuantityResult::from).toList()
            );
        }

        public record WeeklyQuantityAmountResult(
            int quantityAmount,
            String unit,
            String quantityMemo
        ) {
            public static WeeklyQuantityAmountResult of(WeeklyQuantityAmount weeklyQuantityAmount, String unit) {
                return new WeeklyQuantityAmountResult(
                        weeklyQuantityAmount.quantityAmount().value(),
                        unit,
                        weeklyQuantityAmount.quantityMemo().value()
                );
            }
        }

        public record DailyQuantityResult(
                Long dailyQuantityId,
                PlanDateResult planDate,
                DailyQuantityAmountResult dailyQuantityAmount
        ) {

            public static DailyQuantityResult from(WeeklyPlanDailyQuantityData data) {
                return new DailyQuantityResult(
                        data.dailyQuantityId(),
                        PlanDateResult.from(data.planDate()),
                        DailyQuantityAmountResult.from(data.dailyQuantityAmount())
                );
            }
        }
    }
}
