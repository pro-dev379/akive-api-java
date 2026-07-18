package net.akive.api.plan.application.service;

import net.akive.api.plan.application.dto.query.GetSessionalPlanQuery;
import net.akive.api.plan.application.dto.result.common.*;
import net.akive.api.plan.application.dto.result.sessionalplan.GetSessionalPlanDetailResult;
import net.akive.api.plan.application.dto.result.sessionalplan.GetSessionalPlansResult;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.application.port.out.LoadTotalQuantityPort;
import net.akive.api.plan.application.port.out.dto.SessionalPlanTotalQuantityData;
import net.akive.api.plan.application.port.out.dto.SessionalPlanWeeklyQuantityAmountData;
import net.akive.api.plan.application.port.out.dto.SessionalPlanWeeklyQuantityData;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionWeek;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.Subject;
import net.akive.api.plan.domain.TotalQuantityAmount;
import net.akive.api.plan.exception.PlanException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
import static net.akive.api.plan.exception.PlanErrorCode.SESSIONAL_PLAN_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class QuerySessionalPlanServiceTest {

    @Mock
    LoadSessionalPlanPort loadSessionalPlanPort;

    @Mock
    LoadTotalQuantityPort loadTotalQuantityPort;

    @InjectMocks
    QuerySessionalPlanService querySessionalPlanService;

    @Test
    void 회원의_세셔널_플랜_목록을_조회한다() {
        // given
        Long userId = 1L;
        SessionalPlan firstPlan = SessionalPlan.create(
                2L,
                userId,
                "2회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 14)
                )
        );
        SessionalPlan secondPlan = SessionalPlan.create(
                1L,
                userId,
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 7)
                )
        );

        given(loadSessionalPlanPort.loadAllByUserIdOrderBySessionalPlanIdDesc(userId))
                .willReturn(List.of(firstPlan, secondPlan));

        // when
        GetSessionalPlansResult result = querySessionalPlanService.getAll(userId);

        // then
        assertThat(result.sessionalPlans())
                .extracting(
                        GetSessionalPlansResult.SessionalPlanResult::sessionalPlanId,
                        GetSessionalPlansResult.SessionalPlanResult::sessionName,
                        GetSessionalPlansResult.SessionalPlanResult::sessionPeriod
                )
                .containsExactly(
                        tuple(
                                2L,
                                "2회차",
                                new SessionPeriodResult(
                                        LocalDate.of(2026, 7, 8),
                                        LocalDate.of(2026, 7, 14)
                                )
                        ),
                        tuple(
                                1L,
                                "1회차",
                                new SessionPeriodResult(
                                        LocalDate.of(2026, 7, 1),
                                        LocalDate.of(2026, 7, 7)
                                )
                        )
                );

        then(loadSessionalPlanPort).should().loadAllByUserIdOrderBySessionalPlanIdDesc(userId);
    }

    @Test
    void 회원의_세셔널_플랜이_없으면_빈_목록을_반환한다() {
        // given
        Long userId = 1L;

        given(loadSessionalPlanPort.loadAllByUserIdOrderBySessionalPlanIdDesc(userId))
                .willReturn(List.of());

        // when
        GetSessionalPlansResult result = querySessionalPlanService.getAll(userId);

        // then
        assertThat(result.sessionalPlans()).isEmpty();

        then(loadSessionalPlanPort).should().loadAllByUserIdOrderBySessionalPlanIdDesc(userId);
    }

    @Test
    void 회원의_세셔널_플랜_상세를_조회한다() {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        SessionalPlan sessionalPlan = SessionalPlan.create(
                sessionalPlanId,
                userId,
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 6),
                        LocalDate.of(2026, 7, 14)
                )
        );
        List<SessionalPlanTotalQuantityData> totalQuantities = List.of(
                new SessionalPlanTotalQuantityData(
                        10L,
                        sessionalPlanId,
                        new Subject("수학", "학습"),
                        TotalQuantityAmount.of(100, "문제", "총량 메모"),
                        List.of(
                                new SessionalPlanWeeklyQuantityData(
                                        100L,
                                        10L,
                                        SessionWeek.of(
                                                1,
                                                LocalDate.of(2026, 7, 6),
                                                LocalDate.of(2026, 7, 12)
                                        ),
                                        new SessionalPlanWeeklyQuantityAmountData(70, 30, 40, "1주차 메모")
                                ),
                                new SessionalPlanWeeklyQuantityData(
                                        101L,
                                        10L,
                                        SessionWeek.of(
                                                2,
                                                LocalDate.of(2026, 7, 13),
                                                LocalDate.of(2026, 7, 14)
                                        ),
                                        new SessionalPlanWeeklyQuantityAmountData(30, 10, 20, "2주차 메모")
                                )
                        )
                )
        );

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan));
        given(loadTotalQuantityPort.loadSessionalPlanTotalQuantitiesBySessionalPlanId(
                sessionalPlanId,
                sessionalPlan.getSessionPeriod().splitIntoWeeks()
        )).willReturn(totalQuantities);

        // when
        GetSessionalPlanDetailResult result = querySessionalPlanService.get(
                new GetSessionalPlanQuery(userId, sessionalPlanId)
        );

        // then
        assertThat(result.sessionalPlanId()).isEqualTo(sessionalPlanId);
        assertThat(result.sessionName()).isEqualTo("1회차");
        assertThat(result.sessionPeriod()).isEqualTo(new SessionPeriodResult(
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 14)
        ));
        assertThat(result.weeks())
                .extracting(
                        SessionWeekResult::weekNum,
                        SessionWeekResult::weekPeriod
                )
                .containsExactly(
                        tuple(1, new WeekPeriodResult(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12))),
                        tuple(2, new WeekPeriodResult(LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 14)))
                );
        assertThat(result.totalQuantities())
                .extracting(
                        GetSessionalPlanDetailResult.TotalQuantityResult::totalQuantityId,
                        GetSessionalPlanDetailResult.TotalQuantityResult::subject,
                        GetSessionalPlanDetailResult.TotalQuantityResult::totalQuantityAmount
                )
                .containsExactly(tuple(
                        10L,
                        new SubjectResult("수학", "학습"),
                        new TotalQuantityAmountResult(100, "문제", "총량 메모")
                ));
        assertThat(result.totalQuantities().getFirst().weeklyQuantities())
                .extracting(
                        GetSessionalPlanDetailResult.WeeklyQuantityResult::weeklyQuantityId,
                        GetSessionalPlanDetailResult.WeeklyQuantityResult::sessionWeek,
                        GetSessionalPlanDetailResult.WeeklyQuantityResult::weeklyQuantityAmount
                )
                .containsExactly(
                        tuple(
                                100L,
                                new SessionWeekResult(
                                        1,
                                        new WeekPeriodResult(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12))
                                ),
                                new GetSessionalPlanDetailResult.WeeklyQuantityAmountResult(70, 30, 40, "1주차 메모")
                        ),
                        tuple(
                                101L,
                                new SessionWeekResult(
                                        2,
                                        new WeekPeriodResult(LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 14))
                                ),
                                new GetSessionalPlanDetailResult.WeeklyQuantityAmountResult(30, 10, 20, "2주차 메모")
                        )
                );

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(loadTotalQuantityPort).should().loadSessionalPlanTotalQuantitiesBySessionalPlanId(
                sessionalPlanId,
                sessionalPlan.getSessionPeriod().splitIntoWeeks()
        );
    }

    @Test
    void 세셔널_플랜_상세_조회_시_대상_세셔널_플랜이_없으면_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> querySessionalPlanService.get(
                        new GetSessionalPlanQuery(userId, sessionalPlanId)
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(loadTotalQuantityPort).should(never())
                .loadSessionalPlanTotalQuantitiesBySessionalPlanId(any(), any());
    }
}
