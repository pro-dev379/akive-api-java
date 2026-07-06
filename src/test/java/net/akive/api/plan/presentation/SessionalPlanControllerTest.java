package net.akive.api.plan.presentation;

import net.akive.api.plan.application.dto.command.SessionalPlanCreateCommand;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;
import net.akive.api.plan.domain.exception.SessionalPlanException;
import net.akive.api.plan.presentation.dto.request.SessionalPlanCreateRequest;
import net.akive.api.support.PrivateControllerTestSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.SESSION_NAME_ALREADY_EXISTS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 14)
                ),
                new SessionalPlanSummaryResult(
                        1L,
                        "1회차",
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 7)
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
                .andExpect(jsonPath("$.sessionalPlans[0].sessionStartOn").value("2026-07-08"))
                .andExpect(jsonPath("$.sessionalPlans[0].sessionEndOn").value("2026-07-14"))
                .andExpect(jsonPath("$.sessionalPlans[1].sessionalPlanId").value(1L))
                .andExpect(jsonPath("$.sessionalPlans[1].sessionName").value("1회차"))
                .andExpect(jsonPath("$.sessionalPlans[1].sessionStartOn").value("2026-07-01"))
                .andExpect(jsonPath("$.sessionalPlans[1].sessionEndOn").value("2026-07-07"));

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
    void 세셔널_플랜을_생성한다() throws Exception {
        // given
        Long userId = 1L;
        SessionalPlanCreateRequest request = new SessionalPlanCreateRequest(
                "1회차",
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );
        SessionalPlanCreateCommand command = request.toCommand(userId);
        SessionalPlanSummaryResult result = new SessionalPlanSummaryResult(
                1L,
                "1회차",
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
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
        SessionalPlanCreateRequest request = new SessionalPlanCreateRequest(
                "",
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));

        then(createSessionalPlanUseCase).should(never()).create(any(SessionalPlanCreateCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청_시_세셔널_플랜명이_100자를_초과하면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        SessionalPlanCreateRequest request = new SessionalPlanCreateRequest(
                "a".repeat(101),
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));

        then(createSessionalPlanUseCase).should(never()).create(any(SessionalPlanCreateCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청_시_시작일이_없으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        SessionalPlanCreateRequest request = new SessionalPlanCreateRequest(
                "1회차",
                null,
                LocalDate.of(2026, 7, 14)
        );

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));

        then(createSessionalPlanUseCase).should(never()).create(any(SessionalPlanCreateCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청_시_종료일이_없으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        SessionalPlanCreateRequest request = new SessionalPlanCreateRequest(
                "1회차",
                LocalDate.of(2026, 7, 7),
                null
        );

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));

        then(createSessionalPlanUseCase).should(never()).create(any(SessionalPlanCreateCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청_시_날짜_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        String request = """
                {
                  "sessionName": "1회차",
                  "sessionStartOn": "2026/07/07",
                  "sessionEndOn": "2026-07-14"
                }
                """;

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_1"));

        then(createSessionalPlanUseCase).should(never()).create(any(SessionalPlanCreateCommand.class));
    }

    @Test
    void 이미_존재하는_세셔널_플랜명이면_409를_응답한다() throws Exception {
        // given
        Long userId = 1L;
        SessionalPlanCreateRequest request = new SessionalPlanCreateRequest(
                "1회차",
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        willThrow(new SessionalPlanException(SESSION_NAME_ALREADY_EXISTS))
                .given(createSessionalPlanUseCase)
                .create(request.toCommand(userId));

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSIONAL_PLAN_409_1"));
    }
}
