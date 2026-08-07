package net.akive.api.plan.adapter.webapi;

import net.akive.api.plan.application.provided.dto.query.GetWeeklyPlanQuery;
import net.akive.api.plan.application.provided.dto.result.common.DailyQuantityAmountResult;
import net.akive.api.plan.application.provided.dto.result.common.SessionWeekResult;
import net.akive.api.plan.application.provided.dto.result.common.SubjectResult;
import net.akive.api.plan.application.provided.dto.result.common.WeekPeriodResult;
import net.akive.api.plan.application.provided.dto.result.weeklyplan.GetWeeklyPlanResult;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.support.PrivateControllerTestSupport;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.NotFoundError.WEEKLY_PLAN_NOT_FOUND;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WeeklyPlanControllerTest extends PrivateControllerTestSupport {

    @Test
    void 위클리_플랜을_조회한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        Integer weekNum = 1;
        GetWeeklyPlanQuery command = new GetWeeklyPlanQuery(sessionalPlanId, userId, weekNum);
        GetWeeklyPlanResult result = new GetWeeklyPlanResult(
                new SessionWeekResult(
                        weekNum,
                        new WeekPeriodResult(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12))
                ),
                List.of(
                        new GetWeeklyPlanResult.PlanDateResult(LocalDate.of(2026, 7, 6), DayOfWeek.MONDAY),
                        new GetWeeklyPlanResult.PlanDateResult(LocalDate.of(2026, 7, 7), DayOfWeek.TUESDAY)
                ),
                List.of(
                        new GetWeeklyPlanResult.WeeklyQuantityResult(
                                sessionalPlanId,
                                10L,
                                100L,
                                new SubjectResult("수학", "학습"),
                                new GetWeeklyPlanResult.WeeklyQuantityResult.WeeklyQuantityAmountResult(
                                        70,
                                        "문제",
                                        "1주차 메모"
                                ),
                                List.of(
                                        new GetWeeklyPlanResult.WeeklyQuantityResult.DailyQuantityResult(
                                                1000L,
                                                new GetWeeklyPlanResult.PlanDateResult(
                                                        LocalDate.of(2026, 7, 6),
                                                        DayOfWeek.MONDAY
                                                ),
                                                new DailyQuantityAmountResult(30, 10, 20, "월요일 메모")
                                        )
                                )
                        )
                )
        );

        given(getWeeklyPlanUseCase.getByWeekNum(command)).willReturn(result);

        // when & then
        mockMvc.perform(get(
                        "/sessional-plans/{sessionalPlanId}/weekly-plans/{weekNum}",
                        sessionalPlanId,
                        weekNum
                ).with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionWeek.weekNum").value(weekNum))
                .andExpect(jsonPath("$.sessionWeek.weekPeriod.startOn").value("2026-07-06"))
                .andExpect(jsonPath("$.sessionWeek.weekPeriod.endOn").value("2026-07-12"))
                .andExpect(jsonPath("$.planDates[0].date").value("2026-07-06"))
                .andExpect(jsonPath("$.planDates[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.planDates[1].date").value("2026-07-07"))
                .andExpect(jsonPath("$.planDates[1].dayOfWeek").value("TUESDAY"))
                .andExpect(jsonPath("$.weeklyQuantities[0].sessionalPlanId").value(sessionalPlanId))
                .andExpect(jsonPath("$.weeklyQuantities[0].totalQuantityId").value(10L))
                .andExpect(jsonPath("$.weeklyQuantities[0].weeklyQuantityId").value(100L))
                .andExpect(jsonPath("$.weeklyQuantities[0].subject.name").value("수학"))
                .andExpect(jsonPath("$.weeklyQuantities[0].subject.category").value("학습"))
                .andExpect(jsonPath("$.weeklyQuantities[0].weeklyQuantityAmount.quantityAmount").value(70))
                .andExpect(jsonPath("$.weeklyQuantities[0].weeklyQuantityAmount.unit").value("문제"))
                .andExpect(jsonPath("$.weeklyQuantities[0].weeklyQuantityAmount.quantityMemo").value("1주차 메모"))
                .andExpect(jsonPath("$.weeklyQuantities[0].dailyQuantities[0].dailyQuantityId").value(1000L))
                .andExpect(jsonPath("$.weeklyQuantities[0].dailyQuantities[0].planDate.date").value("2026-07-06"))
                .andExpect(jsonPath("$.weeklyQuantities[0].dailyQuantities[0].dailyQuantityAmount.quantityAmount").value(30))
                .andExpect(jsonPath("$.weeklyQuantities[0].dailyQuantities[0].dailyQuantityAmount.completedQuantityAmount").value(10))
                .andExpect(jsonPath("$.weeklyQuantities[0].dailyQuantities[0].dailyQuantityAmount.delayedQuantityAmount").value(20))
                .andExpect(jsonPath("$.weeklyQuantities[0].dailyQuantities[0].dailyQuantityAmount.quantityMemo").value("월요일 메모"));

        then(getWeeklyPlanUseCase).should().getByWeekNum(command);
    }

    @Test
    void 위클리_플랜_조회_시_세셔널_플랜이_없으면_404를_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        Integer weekNum = 1;
        GetWeeklyPlanQuery command = new GetWeeklyPlanQuery(sessionalPlanId, userId, weekNum);

        given(getWeeklyPlanUseCase.getByWeekNum(command))
                .willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND));

        // when & then
        mockMvc.perform(get(
                        "/sessional-plans/{sessionalPlanId}/weekly-plans/{weekNum}",
                        sessionalPlanId,
                        weekNum
                ).with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(SESSIONAL_PLAN_NOT_FOUND.getCode()));

        then(getWeeklyPlanUseCase).should().getByWeekNum(command);
    }

    @Test
    void 위클리_플랜_조회_시_주차가_없으면_404를_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        Integer weekNum = 99;
        GetWeeklyPlanQuery command = new GetWeeklyPlanQuery(sessionalPlanId, userId, weekNum);

        given(getWeeklyPlanUseCase.getByWeekNum(command))
                .willThrow(new NotFoundException(WEEKLY_PLAN_NOT_FOUND));

        // when & then
        mockMvc.perform(get(
                        "/sessional-plans/{sessionalPlanId}/weekly-plans/{weekNum}",
                        sessionalPlanId,
                        weekNum
                ).with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(WEEKLY_PLAN_NOT_FOUND.getCode()));

        then(getWeeklyPlanUseCase).should().getByWeekNum(command);
    }
}
