package net.akive.api.plan.presentation;

import net.akive.api.plan.application.dto.command.GetDailyTodosCommand;
import net.akive.api.plan.application.dto.result.common.SubjectResult;
import net.akive.api.plan.application.dto.result.dailytodo.GetDailyTodosResult;
import net.akive.api.support.PrivateControllerTestSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DailyTodoControllerTest extends PrivateControllerTestSupport {

    @Test
    void 날짜별_투두_리스트를_조회한다() throws Exception {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 7, 7);
        GetDailyTodosResult result = new GetDailyTodosResult(List.of(
                new GetDailyTodosResult.DailyTodoResult(
                        1L,
                        10L,
                        100L,
                        1000L,
                        new SubjectResult("영어", "학습"),
                        new GetDailyTodosResult.DailyTodoResult.DailyTodoAmountResult(30, 10, 20, "분", "메모")
                )
        ));

        given(getDailyTodosUseCase.getByDate(new GetDailyTodosCommand(userId, date)))
                .willReturn(result);

        // when & then
        mockMvc.perform(get("/sessional-plans/todos/{date}", date)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todos[0].sessionalPlanId").value(1L))
                .andExpect(jsonPath("$.todos[0].totalQuantityId").value(10L))
                .andExpect(jsonPath("$.todos[0].weeklyQuantityId").value(100L))
                .andExpect(jsonPath("$.todos[0].dailyQuantityId").value(1000L))
                .andExpect(jsonPath("$.todos[0].subject.name").value("영어"))
                .andExpect(jsonPath("$.todos[0].subject.category").value("학습"))
                .andExpect(jsonPath("$.todos[0].dailyTodoQuantityAmount.quantityAmount").value(30))
                .andExpect(jsonPath("$.todos[0].dailyTodoQuantityAmount.completedQuantityAmount").value(10))
                .andExpect(jsonPath("$.todos[0].dailyTodoQuantityAmount.delayedQuantityAmount").value(20))
                .andExpect(jsonPath("$.todos[0].dailyTodoQuantityAmount.unit").value("분"))
                .andExpect(jsonPath("$.todos[0].dailyTodoQuantityAmount.quantityMemo").value("메모"));

        then(getDailyTodosUseCase).should().getByDate(new GetDailyTodosCommand(userId, date));
    }

    @Test
    void 날짜별_투두_리스트가_없으면_빈_목록을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 7, 7);

        given(getDailyTodosUseCase.getByDate(new GetDailyTodosCommand(userId, date)))
                .willReturn(new GetDailyTodosResult(List.of()));

        // when & then
        mockMvc.perform(get("/sessional-plans/todos/{date}", date)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todos").isArray())
                .andExpect(jsonPath("$.todos").isEmpty());

        then(getDailyTodosUseCase).should().getByDate(new GetDailyTodosCommand(userId, date));
    }

    @Test
    void 날짜별_투두_리스트_요청_시_날짜_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;

        // when & then
        mockMvc.perform(get("/sessional-plans/todos/{date}", "2026-13-07")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_INVALID_INPUT_VALUE"));

        then(getDailyTodosUseCase).should(never()).getByDate(any(GetDailyTodosCommand.class));
    }
}