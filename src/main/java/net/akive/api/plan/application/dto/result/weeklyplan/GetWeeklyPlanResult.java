package net.akive.api.plan.application.dto.result.weeklyplan;

import net.akive.api.plan.application.dto.result.common.DailyQuantityAmountResult;
import net.akive.api.plan.application.dto.result.common.SessionWeekResult;
import net.akive.api.plan.application.dto.result.common.SubjectResult;
import net.akive.api.plan.application.port.out.dto.WeeklyPlanDailyQuantityData;
import net.akive.api.plan.application.port.out.dto.WeeklyPlanWeeklyQuantityData;
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

        public static WeeklyQuantityResult from(WeeklyPlanWeeklyQuantityData weeklyPlanWeeklyQuantityData) {
            return new WeeklyQuantityResult(
                    weeklyPlanWeeklyQuantityData.sessionalPlanId(),
                    weeklyPlanWeeklyQuantityData.totalQuantityId(),
                    weeklyPlanWeeklyQuantityData.weeklyQuantityId(),
                    SubjectResult.from(weeklyPlanWeeklyQuantityData.subject()),
                    WeeklyQuantityAmountResult.of(weeklyPlanWeeklyQuantityData.weeklyQuantityAmount(), weeklyPlanWeeklyQuantityData.unit()),
                    weeklyPlanWeeklyQuantityData.dailyQuantities().stream().map(DailyQuantityResult::from).toList()
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

            public static DailyQuantityResult from(WeeklyPlanDailyQuantityData weeklyPlanDailyQuantityData) {
                return new DailyQuantityResult(
                        weeklyPlanDailyQuantityData.dailyQuantityId(),
                        PlanDateResult.from(weeklyPlanDailyQuantityData.planDate()),
                        DailyQuantityAmountResult.from(weeklyPlanDailyQuantityData.dailyQuantityAmount())
                );
            }
        }
    }
}