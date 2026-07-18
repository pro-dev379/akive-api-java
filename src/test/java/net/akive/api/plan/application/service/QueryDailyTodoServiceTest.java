package net.akive.api.plan.application.service;

import net.akive.api.plan.application.dto.command.GetDailyTodosCommand;
import net.akive.api.plan.application.dto.result.dailytodo.GetDailyTodosResult;
import net.akive.api.plan.application.port.out.LoadDailyTodosPort;
import net.akive.api.plan.application.port.out.dto.DailyTodoData;
import net.akive.api.plan.domain.DailyQuantityAmount;
import net.akive.api.plan.domain.Subject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class QueryDailyTodoServiceTest {

    @Mock
    LoadDailyTodosPort loadDailyTodosPort;

    @InjectMocks
    QueryDailyTodoService queryDailyTodoService;

    @Test
    void 회원의_날짜별_투두_리스트를_조회한다() {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 7, 7);
        List<DailyTodoData> dailyTodos = List.of(
                new DailyTodoData(
                        1L,
                        10L,
                        100L,
                        1000L,
                        new Subject("영어", "학습"),
                        DailyQuantityAmount.of(30, 10, 20, "단어 암기"),
                        "분",
                        date
                )
        );
        GetDailyTodosCommand command = new GetDailyTodosCommand(userId, date);

        given(loadDailyTodosPort.loadDailyTodosByUserIdAndDate(userId, date))
                .willReturn(dailyTodos);

        // when
        GetDailyTodosResult result = queryDailyTodoService.getByDate(command);

        // then
        assertThat(result.todos()).isEqualTo(dailyTodos.stream().map(GetDailyTodosResult.DailyTodoResult::from).toList());

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
        GetDailyTodosResult result = queryDailyTodoService.getByDate(command);

        // then
        assertThat(result.todos()).isEqualTo(List.of());

        then(loadDailyTodosPort).should().loadDailyTodosByUserIdAndDate(userId, date);
    }
}