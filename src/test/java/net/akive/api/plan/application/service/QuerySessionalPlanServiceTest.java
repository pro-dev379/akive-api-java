package net.akive.api.plan.application.service;

import net.akive.api.plan.application.dto.command.GetDailyTodosCommand;
import net.akive.api.plan.application.dto.result.DailyTodoResult;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;
import net.akive.api.plan.application.port.out.LoadDailyTodosPort;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionalPlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class QuerySessionalPlanServiceTest {

    @Mock
    LoadSessionalPlanPort loadSessionalPlanPort;

    @Mock
    LoadDailyTodosPort loadDailyTodosPort;

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
        List<SessionalPlanSummaryResult> results = querySessionalPlanService.getAll(userId);

        // then
        assertThat(results)
                .extracting(
                        SessionalPlanSummaryResult::sessionalPlanId,
                        SessionalPlanSummaryResult::sessionName,
                        SessionalPlanSummaryResult::sessionPeriod
                )
                .containsExactly(
                        tuple(
                                2L,
                                "2회차",
                                new SessionPeriod(
                                        LocalDate.of(2026, 7, 8),
                                        LocalDate.of(2026, 7, 14)
                                )
                        ),
                        tuple(
                                1L,
                                "1회차",
                                new SessionPeriod(
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
        List<SessionalPlanSummaryResult> results = querySessionalPlanService.getAll(userId);

        // then
        assertThat(results).isEmpty();

        then(loadSessionalPlanPort).should().loadAllByUserIdOrderBySessionalPlanIdDesc(userId);
    }

    @Test
    void 회원의_날짜별_투두_리스트를_조회한다() {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 7, 7);
        List<DailyTodoResult> dailyTodos = List.of(
                new DailyTodoResult(
                        1L,
                        10L,
                        100L,
                        1000L,
                        "영어",
                        "학습",
                        30,
                        10,
                        0,
                        "분",
                        "단어 암기",
                        date
                )
        );
        GetDailyTodosCommand command = new GetDailyTodosCommand(userId, date);

        given(loadDailyTodosPort.loadDailyTodosByUserIdAndDate(userId, date))
                .willReturn(dailyTodos);

        // when
        List<DailyTodoResult> results = querySessionalPlanService.getTodosByDate(command);

        // then
        assertThat(results).isEqualTo(dailyTodos);

        then(loadDailyTodosPort).should().loadDailyTodosByUserIdAndDate(userId, date);
    }

    @Test
    void 회원의_날짜별_투두_리스트가_없으면_빈_목록을_반환한다() {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 7, 7);
        GetDailyTodosCommand command = new GetDailyTodosCommand(userId, date);

        given(loadDailyTodosPort.loadDailyTodosByUserIdAndDate(userId, date))
                .willReturn(List.of());

        // when
        List<DailyTodoResult> results = querySessionalPlanService.getTodosByDate(command);

        // then
        assertThat(results).isEqualTo(List.of());

        then(loadDailyTodosPort).should().loadDailyTodosByUserIdAndDate(userId, date);
    }
}
