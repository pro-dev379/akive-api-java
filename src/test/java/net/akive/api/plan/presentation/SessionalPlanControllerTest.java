package net.akive.api.plan.presentation;

import net.akive.api.plan.application.dto.command.CreateSessionalPlanCommand;
import net.akive.api.plan.application.dto.command.DeleteSessionalPlanCommand;
import net.akive.api.plan.application.dto.command.GetDailyTodosCommand;
import net.akive.api.plan.application.dto.command.UpdateSessionalPlanCommand;
import net.akive.api.plan.application.dto.result.DailyTodoResult;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.exception.SessionalPlanException;
import net.akive.api.plan.presentation.dto.request.CreateSessionalPlanRequest;
import net.akive.api.plan.presentation.dto.request.SessionPeriodRequest;
import net.akive.api.plan.presentation.dto.request.UpdateSessionalPlanRequest;
import net.akive.api.support.PrivateControllerTestSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.NOT_FOUND;
import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.SESSION_NAME_ALREADY_EXISTS;
import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.SESSION_PERIOD_CHANGE_NOT_ALLOWED;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SessionalPlanControllerTest extends PrivateControllerTestSupport {

    @Test
    void 세셔널_플랜_목록을_조회한다() throws Exception {
        // given
        Long userId = 1L;
        List<SessionalPlanSummaryResult> results = List.of(
                new SessionalPlanSummaryResult(
                        2L,
                        "2회차",
                        new SessionPeriod(
                                LocalDate.of(2026, 7, 8),
                                LocalDate.of(2026, 7, 14)
                        )
                ),
                new SessionalPlanSummaryResult(
                        1L,
                        "1회차",
                        new SessionPeriod(
                                LocalDate.of(2026, 7, 1),
                                LocalDate.of(2026, 7, 7)
                        )
                )
        );

        given(getSessionalPlansUseCase.getAll(userId))
                .willReturn(results);

        // when & then
        mockMvc.perform(get("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionalPlans[0].sessionalPlanId").value(2L))
                .andExpect(jsonPath("$.sessionalPlans[0].sessionName").value("2회차"))
                .andExpect(jsonPath("$.sessionalPlans[0].sessionPeriod.startOn").value("2026-07-08"))
                .andExpect(jsonPath("$.sessionalPlans[0].sessionPeriod.endOn").value("2026-07-14"))
                .andExpect(jsonPath("$.sessionalPlans[1].sessionalPlanId").value(1L))
                .andExpect(jsonPath("$.sessionalPlans[1].sessionName").value("1회차"))
                .andExpect(jsonPath("$.sessionalPlans[1].sessionPeriod.startOn").value("2026-07-01"))
                .andExpect(jsonPath("$.sessionalPlans[1].sessionPeriod.endOn").value("2026-07-07"));

        then(getSessionalPlansUseCase).should().getAll(userId);
    }

    @Test
    void 세셔널_플랜이_없으면_빈_목록을_응답한다() throws Exception {
        // given
        Long userId = 1L;

        given(getSessionalPlansUseCase.getAll(userId))
                .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionalPlans").isArray())
                .andExpect(jsonPath("$.sessionalPlans").isEmpty());

        then(getSessionalPlansUseCase).should().getAll(userId);
    }

    @Test
    void 날짜별_투두_리스트를_조회한다() throws Exception {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 7, 7);
        List<DailyTodoResult> results = List.of(
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

        given(getDailyTodosUseCase.getTodosByDate(new GetDailyTodosCommand(userId, date)))
                .willReturn(results);

        // when & then
        mockMvc.perform(get("/sessional-plans/todos/{date}", date)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todos[0].sessionalPlanId").value(1L))
                .andExpect(jsonPath("$.todos[0].totalQuantityId").value(10L))
                .andExpect(jsonPath("$.todos[0].weeklyQuantityId").value(100L))
                .andExpect(jsonPath("$.todos[0].dailyQuantityId").value(1000L))
                .andExpect(jsonPath("$.todos[0].subject").value("영어"))
                .andExpect(jsonPath("$.todos[0].subjectCategory").value("학습"))
                .andExpect(jsonPath("$.todos[0].dailyQuantityAmount").value(30))
                .andExpect(jsonPath("$.todos[0].completedQuantityAmount").value(10))
                .andExpect(jsonPath("$.todos[0].delayQuantityAmount").value(0))
                .andExpect(jsonPath("$.todos[0].unit").value("분"))
                .andExpect(jsonPath("$.todos[0].dailyQuantityMemo").value("단어 암기"))
                .andExpect(jsonPath("$.todos[0].date").value("2026-07-07"));

        then(getDailyTodosUseCase).should().getTodosByDate(new GetDailyTodosCommand(userId, date));
    }

    @Test
    void 날짜별_투두_리스트가_없으면_빈_목록을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 7, 7);

        given(getDailyTodosUseCase.getTodosByDate(new GetDailyTodosCommand(userId, date)))
                .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/sessional-plans/todos/{date}", date)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todos").isArray())
                .andExpect(jsonPath("$.todos").isEmpty());

        then(getDailyTodosUseCase).should().getTodosByDate(new GetDailyTodosCommand(userId, date));
    }

    @Test
    void 날짜별_투두_리스트_요청_시_날짜_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;

        // when & then
        mockMvc.perform(get("/sessional-plans/todos/{date}", "2026-13-07")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));

        then(getDailyTodosUseCase).should(never()).getTodosByDate(any(GetDailyTodosCommand.class));
    }

    @Test
    void 세셔널_플랜을_생성한다() throws Exception {
        // given
        Long userId = 1L;
        CreateSessionalPlanRequest request = new CreateSessionalPlanRequest(
                "1회차",
                new SessionPeriodRequest(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
        CreateSessionalPlanCommand command = request.to(userId);
        SessionalPlanSummaryResult result = new SessionalPlanSummaryResult(
                1L,
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );

        given(createSessionalPlanUseCase.create(command))
                .willReturn(result);

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionalPlanId").value(1L));

        then(createSessionalPlanUseCase).should().create(command);
    }


    @Test
    void 세셔널_플랜_생성_요청_시_세셔널_플랜명이_비어있으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        CreateSessionalPlanRequest request = new CreateSessionalPlanRequest(
                "",
                new SessionPeriodRequest(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));

        then(createSessionalPlanUseCase).should(never()).create(any(CreateSessionalPlanCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청_시_세셔널_플랜명이_100자를_초과하면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        CreateSessionalPlanRequest request = new CreateSessionalPlanRequest(
                "a".repeat(101),
                new SessionPeriodRequest(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));

        then(createSessionalPlanUseCase).should(never()).create(any(CreateSessionalPlanCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청_시_세션_기간이_없으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        CreateSessionalPlanRequest request = new CreateSessionalPlanRequest(
                "1회차",
                null
        );

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));

        then(createSessionalPlanUseCase).should(never()).create(any(CreateSessionalPlanCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청_시_시작일이_없으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        CreateSessionalPlanRequest request = new CreateSessionalPlanRequest(
                "1회차",
                new SessionPeriodRequest(
                        null,
                        LocalDate.of(2026, 7, 14)
                )
        );

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(REQUIRED_VALUE_MISSING.getCode()));

        then(createSessionalPlanUseCase).should(never()).create(any(CreateSessionalPlanCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청_시_종료일이_없으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        CreateSessionalPlanRequest request = new CreateSessionalPlanRequest(
                "1회차",
                new SessionPeriodRequest(
                        LocalDate.of(2026, 7, 7),
                        null
                )
        );

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(REQUIRED_VALUE_MISSING.getCode()));

        then(createSessionalPlanUseCase).should(never()).create(any(CreateSessionalPlanCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청_시_날짜_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        String request = """
                {
                  "sessionName": "1회차",
                  "sessionPeriod": {
                    "startOn": "2026/07/07",
                    "endOn": "2026-07-14"
                  }
                }
                """;

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));

        then(createSessionalPlanUseCase).should(never()).create(any(CreateSessionalPlanCommand.class));
    }

    @Test
    void 이미_존재하는_세셔널_플랜명이면_409를_응답한다() throws Exception {
        // given
        Long userId = 1L;
        CreateSessionalPlanRequest request = new CreateSessionalPlanRequest(
                "1회차",
                new SessionPeriodRequest(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );

        willThrow(new SessionalPlanException(SESSION_NAME_ALREADY_EXISTS))
                .given(createSessionalPlanUseCase)
                .create(request.to(userId));

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSIONAL_PLAN_409_1"));
    }

    @Test
    void 세셔널_플랜을_수정한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanRequest request = new UpdateSessionalPlanRequest(
                "수정된 1회차",
                new SessionPeriodRequest(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        then(updateSessionalPlanUseCase).should()
                .update(request.to(sessionalPlanId, userId));
    }

    @Test
    void 세셔널_플랜_수정_요청_시_세셔널_플랜명이_비어있으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanRequest request = new UpdateSessionalPlanRequest(
                "",
                new SessionPeriodRequest(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));

        then(updateSessionalPlanUseCase).should(never()).update(any(UpdateSessionalPlanCommand.class));
    }

    @Test
    void 세셔널_플랜_수정_요청_시_세셔널_플랜명이_100자를_초과하면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanRequest request = new UpdateSessionalPlanRequest(
                "a".repeat(101),
                new SessionPeriodRequest(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));

        then(updateSessionalPlanUseCase).should(never()).update(any(UpdateSessionalPlanCommand.class));
    }

    @Test
    void 세셔널_플랜_수정_요청_시_세션_기간이_없으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanRequest request = new UpdateSessionalPlanRequest(
                "수정된 1회차",
                null
        );

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));

        then(updateSessionalPlanUseCase).should(never()).update(any(UpdateSessionalPlanCommand.class));
    }

    @Test
    void 세셔널_플랜_수정_요청_시_시작일이_없으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanRequest request = new UpdateSessionalPlanRequest(
                "수정된 1회차",
                new SessionPeriodRequest(
                        null,
                        LocalDate.of(2026, 7, 15)
                )
        );

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(REQUIRED_VALUE_MISSING.getCode()));

        then(updateSessionalPlanUseCase).should(never()).update(any(UpdateSessionalPlanCommand.class));
    }

    @Test
    void 세셔널_플랜_수정_요청_시_종료일이_없으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanRequest request = new UpdateSessionalPlanRequest(
                "수정된 1회차",
                new SessionPeriodRequest(
                        LocalDate.of(2026, 7, 8),
                        null
                )
        );

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(REQUIRED_VALUE_MISSING.getCode()));

        then(updateSessionalPlanUseCase).should(never()).update(any(UpdateSessionalPlanCommand.class));
    }

    @Test
    void 세셔널_플랜_수정_요청_시_날짜_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        String request = """
                {
                  "sessionName": "1회차",
                  "sessionPeriod": {
                    "startOn": "2026/07/07",
                    "endOn": "2026-07-14"
                  }
                }
                """;

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));

        then(createSessionalPlanUseCase).should(never()).create(any(CreateSessionalPlanCommand.class));
    }

    @Test
    void 수정할_세셔널_플랜이_없으면_404를_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanRequest request = new UpdateSessionalPlanRequest(
                "수정된 1회차",
                new SessionPeriodRequest(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );

        willThrow(new SessionalPlanException(NOT_FOUND))
                .given(updateSessionalPlanUseCase)
                .update(request.to(sessionalPlanId, userId));

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSIONAL_PLAN_404_1"));

        then(updateSessionalPlanUseCase).should().update(request.to(sessionalPlanId, userId));
    }

    @Test
    void 이미_존재하는_세셔널_플랜명으로_수정하면_409를_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanRequest request = new UpdateSessionalPlanRequest(
                "2회차",
                new SessionPeriodRequest(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );

        willThrow(new SessionalPlanException(SESSION_NAME_ALREADY_EXISTS))
                .given(updateSessionalPlanUseCase)
                .update(request.to(sessionalPlanId, userId));

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSIONAL_PLAN_409_1"));

        then(updateSessionalPlanUseCase).should().update(request.to(sessionalPlanId, userId));
    }

    @Test
    void 세션_총량이_등록된_세셔널_플랜의_기간을_수정하면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanRequest request = new UpdateSessionalPlanRequest(
                "1회차",
                new SessionPeriodRequest(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );

        willThrow(new SessionalPlanException(SESSION_PERIOD_CHANGE_NOT_ALLOWED))
                .given(updateSessionalPlanUseCase)
                .update(request.to(sessionalPlanId, userId));

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SESSIONAL_PLAN_400_2"));

        then(updateSessionalPlanUseCase).should().update(request.to(sessionalPlanId, userId));
    }

    @Test
    void 세션_총량이_등록되어도_기간을_변경하지_않으면_세셔널_플랜명을_수정한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanRequest request = new UpdateSessionalPlanRequest(
                "수정된 1회차",
                new SessionPeriodRequest(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        then(updateSessionalPlanUseCase).should()
                .update(request.to(sessionalPlanId, userId));
    }

    @Test
    void 세셔널_플랜을_삭제한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;

        // when & then
        mockMvc.perform(delete("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isNoContent());

        then(deleteSessionalPlanUseCase).should()
                .delete(new DeleteSessionalPlanCommand(userId, sessionalPlanId));
    }

    @Test
    void 삭제할_세셔널_플랜이_없으면_404를_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        DeleteSessionalPlanCommand command = new DeleteSessionalPlanCommand(userId, sessionalPlanId);

        willThrow(new SessionalPlanException(NOT_FOUND))
                .given(deleteSessionalPlanUseCase)
                .delete(command);

        // when & then
        mockMvc.perform(delete("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSIONAL_PLAN_404_1"));

        then(deleteSessionalPlanUseCase).should().delete(command);
    }
}
