package net.akive.api.plan.application.service;

import net.akive.api.plan.application.dto.query.GetWeeklyPlanQuery;
import net.akive.api.plan.application.dto.result.common.DailyQuantityAmountResult;
import net.akive.api.plan.application.dto.result.common.SessionWeekResult;
import net.akive.api.plan.application.dto.result.common.SubjectResult;
import net.akive.api.plan.application.dto.result.common.WeekPeriodResult;
import net.akive.api.plan.application.dto.result.weeklyplan.GetWeeklyPlanResult;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.application.port.out.LoadWeeklyQuantityPort;
import net.akive.api.plan.application.port.out.dto.WeeklyPlanDailyQuantityData;
import net.akive.api.plan.application.port.out.dto.WeeklyPlanWeeklyQuantityData;
import net.akive.api.plan.domain.DailyQuantityAmount;
import net.akive.api.plan.domain.PlanDate;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.Subject;
import net.akive.api.plan.domain.WeekPeriod;
import net.akive.api.plan.domain.WeeklyQuantityAmount;
import net.akive.api.plan.exception.PlanException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static net.akive.api.plan.exception.PlanErrorCode.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.plan.exception.PlanErrorCode.WEEKLY_PLAN_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class QueryWeeklyPlanServiceTest {

    @Mock
    LoadSessionalPlanPort loadSessionalPlanPort;

    @Mock
    LoadWeeklyQuantityPort loadWeeklyQuantityPort;

    @InjectMocks
    QueryWeeklyPlanService queryWeeklyPlanService;

    @Test
    void 위클리_플랜을_조회한다() {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        Integer weekNum = 1;
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 14)
        );
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 12)
        );
        List<WeeklyPlanWeeklyQuantityData> weeklyQuantities = List.of(
                new WeeklyPlanWeeklyQuantityData(
                        sessionalPlanId,
                        10L,
                        100L,
                        new Subject("수학", "학습"),
                        WeeklyQuantityAmount.of(70, "1주차 메모"),
                        "문제",
                        List.of(
                                new WeeklyPlanDailyQuantityData(
                                        1000L,
                                        DailyQuantityAmount.of(30, 10, 20, "월요일 메모"),
                                        PlanDate.from(LocalDate.of(2026, 7, 6))
                                ),
                                new WeeklyPlanDailyQuantityData(
                                        1001L,
                                        DailyQuantityAmount.of(40, 20, 20, "화요일 메모"),
                                        PlanDate.from(LocalDate.of(2026, 7, 7))
                                )
                        )
                )
        );

        given(loadSessionalPlanPort.loadSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionPeriod));
        given(loadWeeklyQuantityPort.loadWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
                sessionalPlanId,
                weekPeriod
        )).willReturn(weeklyQuantities);

        // when
        GetWeeklyPlanResult result = queryWeeklyPlanService.getByWeekNum(
                new GetWeeklyPlanQuery(sessionalPlanId, userId, weekNum)
        );

        // then
        assertThat(result.sessionWeek()).isEqualTo(
                new SessionWeekResult(
                        weekNum,
                        new WeekPeriodResult(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12))
                )
        );
        assertThat(result.planDates())
                .extracting(GetWeeklyPlanResult.PlanDateResult::date)
                .containsExactly(
                        LocalDate.of(2026, 7, 6),
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 9),
                        LocalDate.of(2026, 7, 10),
                        LocalDate.of(2026, 7, 11),
                        LocalDate.of(2026, 7, 12)
                );
        assertThat(result.weeklyQuantities())
                .extracting(
                        GetWeeklyPlanResult.WeeklyQuantityResult::sessionalPlanId,
                        GetWeeklyPlanResult.WeeklyQuantityResult::totalQuantityId,
                        GetWeeklyPlanResult.WeeklyQuantityResult::weeklyQuantityId,
                        GetWeeklyPlanResult.WeeklyQuantityResult::subject,
                        GetWeeklyPlanResult.WeeklyQuantityResult::weeklyQuantityAmount
                )
                .containsExactly(tuple(
                        sessionalPlanId,
                        10L,
                        100L,
                        new SubjectResult("수학", "학습"),
                        new GetWeeklyPlanResult.WeeklyQuantityResult.WeeklyQuantityAmountResult(
                                70,
                                "문제",
                                "1주차 메모"
                        )
                ));
        assertThat(result.weeklyQuantities().getFirst().dailyQuantities())
                .extracting(
                        GetWeeklyPlanResult.WeeklyQuantityResult.DailyQuantityResult::dailyQuantityId,
                        GetWeeklyPlanResult.WeeklyQuantityResult.DailyQuantityResult::planDate,
                        GetWeeklyPlanResult.WeeklyQuantityResult.DailyQuantityResult::dailyQuantityAmount
                )
                .containsExactly(
                        tuple(
                                1000L,
                                new GetWeeklyPlanResult.PlanDateResult(
                                        LocalDate.of(2026, 7, 6),
                                        PlanDate.from(LocalDate.of(2026, 7, 6)).dayOfWeek()
                                ),
                                new DailyQuantityAmountResult(30, 10, 20, "월요일 메모")
                        ),
                        tuple(
                                1001L,
                                new GetWeeklyPlanResult.PlanDateResult(
                                        LocalDate.of(2026, 7, 7),
                                        PlanDate.from(LocalDate.of(2026, 7, 7)).dayOfWeek()
                                ),
                                new DailyQuantityAmountResult(40, 20, 20, "화요일 메모")
                        )
                );

        then(loadSessionalPlanPort).should()
                .loadSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(loadWeeklyQuantityPort).should()
                .loadWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(sessionalPlanId, weekPeriod);
    }

    @Test
    void 위클리_플랜의_주간_계획량이_없으면_빈_목록을_반환한다() {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        Integer weekNum = 2;
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 14)
        );
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2026, 7, 13),
                LocalDate.of(2026, 7, 14)
        );

        given(loadSessionalPlanPort.loadSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionPeriod));
        given(loadWeeklyQuantityPort.loadWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
                sessionalPlanId,
                weekPeriod
        )).willReturn(List.of());

        // when
        GetWeeklyPlanResult result = queryWeeklyPlanService.getByWeekNum(
                new GetWeeklyPlanQuery(sessionalPlanId, userId, weekNum)
        );

        // then
        assertThat(result.sessionWeek()).isEqualTo(
                new SessionWeekResult(
                        weekNum,
                        new WeekPeriodResult(LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 14))
                )
        );
        assertThat(result.planDates())
                .extracting(GetWeeklyPlanResult.PlanDateResult::date)
                .containsExactly(LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 14));
        assertThat(result.weeklyQuantities()).isEmpty();

        then(loadSessionalPlanPort).should()
                .loadSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(loadWeeklyQuantityPort).should()
                .loadWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(sessionalPlanId, weekPeriod);
    }

    @Test
    void 위클리_플랜_조회_시_세셔널_플랜이_없으면_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;

        given(loadSessionalPlanPort.loadSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> queryWeeklyPlanService.getByWeekNum(
                        new GetWeeklyPlanQuery(sessionalPlanId, userId, 1)
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(loadSessionalPlanPort).should()
                .loadSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(loadWeeklyQuantityPort).should(never())
                .loadWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(any(), any());
    }

    @Test
    void 위클리_플랜_조회_시_주차가_세션_기간에_없으면_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 14)
        );

        given(loadSessionalPlanPort.loadSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionPeriod));

        // when & then
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> queryWeeklyPlanService.getByWeekNum(
                        new GetWeeklyPlanQuery(sessionalPlanId, userId, 99)
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(WEEKLY_PLAN_NOT_FOUND);

        then(loadSessionalPlanPort).should()
                .loadSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(loadWeeklyQuantityPort).should(never())
                .loadWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(any(), any());
    }
}