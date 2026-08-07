package net.akive.api.plan.adapter.webapi;

import net.akive.api.plan.application.provided.dto.command.CreateSessionalPlanCommand;
import net.akive.api.plan.application.provided.dto.command.DeleteSessionalPlanCommand;
import net.akive.api.plan.application.provided.dto.query.GetSessionalPlanQuery;
import net.akive.api.plan.application.provided.dto.command.UpdateSessionalPlanCommand;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.apapter.webapi.dto.request.CreateSessionalPlanRequest;
import net.akive.api.plan.apapter.webapi.dto.request.common.SessionPeriodRequest;
import net.akive.api.plan.apapter.webapi.dto.request.UpdateSessionalPlanRequest;
import net.akive.api.plan.application.provided.dto.result.common.*;
import net.akive.api.plan.application.provided.dto.result.sessionalplan.GetSessionalPlanResult;
import net.akive.api.plan.application.provided.dto.result.sessionalplan.GetSessionalPlansResult;
import net.akive.api.support.PrivateControllerTestSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.ConflictError.SESSION_NAME_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.BadRequestError.SESSION_PERIOD_CHANGE_NOT_ALLOWED;
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
        GetSessionalPlansResult result = new GetSessionalPlansResult(List.of(
                new GetSessionalPlansResult.SessionalPlanResult(
                        2L,
                        "2회차",
                        new SessionPeriodResult(
                                LocalDate.of(2026, 7, 8),
                                LocalDate.of(2026, 7, 14)
                        )
                ),
                new GetSessionalPlansResult.SessionalPlanResult(
                        1L,
                        "1회차",
                        new SessionPeriodResult(
                                LocalDate.of(2026, 7, 1),
                                LocalDate.of(2026, 7, 7)
                        )
                )
        ));

        given(getSessionalPlansUseCase.getAll(userId))
                .willReturn(result);

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
                .willReturn(new GetSessionalPlansResult(List.of()));

        // when & then
        mockMvc.perform(get("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionalPlans").isArray())
                .andExpect(jsonPath("$.sessionalPlans").isEmpty());

        then(getSessionalPlansUseCase).should().getAll(userId);
    }

    @Test
    void 세셔널_플랜_상세를_조회한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        GetSessionalPlanQuery command = new GetSessionalPlanQuery(userId, sessionalPlanId);
        GetSessionalPlanResult result = new GetSessionalPlanResult(
                sessionalPlanId,
                "1회차",
                new SessionPeriodResult(
                        LocalDate.of(2026, 7, 6),
                        LocalDate.of(2026, 7, 14)
                ),
                List.of(
                        new SessionWeekResult(
                                1,
                                new WeekPeriodResult(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12))
                        ),
                        new SessionWeekResult(
                                2,
                                new WeekPeriodResult(LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 14))
                        )
                ),
                List.of(
                        new GetSessionalPlanResult.TotalQuantityResult(
                                10L,
                                new SubjectResult("수학", "학습"),
                                new TotalQuantityAmountResult(100, "문제", "총량 메모"),
                                List.of(
                                        new GetSessionalPlanResult.WeeklyQuantityResult(
                                                100L,
                                                new SessionWeekResult(
                                                        1,
                                                        new WeekPeriodResult(
                                                                LocalDate.of(2026, 7, 6),
                                                                LocalDate.of(2026, 7, 12)
                                                        )
                                                ),
                                                new GetSessionalPlanResult.WeeklyQuantityAmountResult(
                                                        70,
                                                        30,
                                                        40,
                                                        "1주차 메모"
                                                )
                                        )
                                )
                        )
                )
        );

        given(getSessionalPlanUseCase.get(command))
                .willReturn(result);

        // when & then
        mockMvc.perform(get("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionalPlanId").value(sessionalPlanId))
                .andExpect(jsonPath("$.sessionName").value("1회차"))
                .andExpect(jsonPath("$.sessionPeriod.startOn").value("2026-07-06"))
                .andExpect(jsonPath("$.sessionPeriod.endOn").value("2026-07-14"))
                .andExpect(jsonPath("$.sessionWeeks[0].weekNum").value(1))
                .andExpect(jsonPath("$.sessionWeeks[0].weekPeriod.startOn").value("2026-07-06"))
                .andExpect(jsonPath("$.sessionWeeks[0].weekPeriod.endOn").value("2026-07-12"))
                .andExpect(jsonPath("$.sessionWeeks[1].weekNum").value(2))
                .andExpect(jsonPath("$.sessionWeeks[1].weekPeriod.startOn").value("2026-07-13"))
                .andExpect(jsonPath("$.sessionWeeks[1].weekPeriod.endOn").value("2026-07-14"))
                .andExpect(jsonPath("$.totalQuantities[0].totalQuantityId").value(10L))
                .andExpect(jsonPath("$.totalQuantities[0].subject.name").value("수학"))
                .andExpect(jsonPath("$.totalQuantities[0].subject.category").value("학습"))
                .andExpect(jsonPath("$.totalQuantities[0].totalQuantityAmount.quantityAmount").value(100))
                .andExpect(jsonPath("$.totalQuantities[0].totalQuantityAmount.unit").value("문제"))
                .andExpect(jsonPath("$.totalQuantities[0].totalQuantityAmount.quantityMemo").value("총량 메모"))
                .andExpect(jsonPath("$.totalQuantities[0].weeklyQuantities[0].weeklyQuantityId").value(100L))
                .andExpect(jsonPath("$.totalQuantities[0].weeklyQuantities[0].sessionWeek.weekNum").value(1))
                .andExpect(jsonPath("$.totalQuantities[0].weeklyQuantities[0].weeklyQuantityAmount.quantityAmount").value(70))
                .andExpect(jsonPath("$.totalQuantities[0].weeklyQuantities[0].weeklyQuantityAmount.completedQuantityAmount").value(30))
                .andExpect(jsonPath("$.totalQuantities[0].weeklyQuantities[0].weeklyQuantityAmount.delayedQuantityAmount").value(40))
                .andExpect(jsonPath("$.totalQuantities[0].weeklyQuantities[0].weeklyQuantityAmount.quantityMemo").value("1주차 메모"));

        then(getSessionalPlanUseCase).should().get(command);
    }

    @Test
    void 세셔널_플랜_상세_조회_시_대상이_없으면_404를_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        GetSessionalPlanQuery command = new GetSessionalPlanQuery(userId, sessionalPlanId);

        given(getSessionalPlanUseCase.get(command))
                .willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(SESSIONAL_PLAN_NOT_FOUND.getCode()));

        then(getSessionalPlanUseCase).should().get(command);
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

        given(createSessionalPlanUseCase.create(command))
                .willReturn(1L);

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
                .andExpect(jsonPath("$.code").value("COMMON_400_INVALID_INPUT_VALUE"));

        then(createSessionalPlanUseCase).should(never()).create(any(CreateSessionalPlanCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청_시_세셔널_플랜명이_50자를_초과하면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        CreateSessionalPlanRequest request = new CreateSessionalPlanRequest(
                "a".repeat(51),
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
                .andExpect(jsonPath("$.code").value("COMMON_400_INVALID_INPUT_VALUE"));

        then(createSessionalPlanUseCase).should(never()).create(any(CreateSessionalPlanCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청_시_세션_기간이_없으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        CreateSessionalPlanRequest request = new CreateSessionalPlanRequest("1회차", null);

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_INVALID_INPUT_VALUE"));

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
                .andExpect(jsonPath("$.code").value("COMMON_400_INVALID_INPUT_VALUE"));

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
                .andExpect(jsonPath("$.code").value("COMMON_400_INVALID_INPUT_VALUE"));

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
                .andExpect(jsonPath("$.code").value("COMMON_400_INVALID_INPUT_VALUE"));

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

        willThrow(new ConflictException(SESSION_NAME_ALREADY_EXISTS))
                .given(createSessionalPlanUseCase)
                .create(request.to(userId));

        // when & then
        mockMvc.perform(post("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PLAN_409_SESSION_NAME_ALREADY_EXISTS"));
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
                .andExpect(jsonPath("$.code").value("COMMON_400_INVALID_INPUT_VALUE"));

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
                .andExpect(jsonPath("$.code").value("COMMON_400_INVALID_INPUT_VALUE"));

        then(updateSessionalPlanUseCase).should(never()).update(any(UpdateSessionalPlanCommand.class));
    }

    @Test
    void 세셔널_플랜_수정_요청_시_세션_기간이_없으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanRequest request = new UpdateSessionalPlanRequest("수정된 1회차", null);

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_INVALID_INPUT_VALUE"));

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
                .andExpect(jsonPath("$.code").value("COMMON_400_INVALID_INPUT_VALUE"));

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
                .andExpect(jsonPath("$.code").value("COMMON_400_INVALID_INPUT_VALUE"));

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
                .andExpect(jsonPath("$.code").value("COMMON_400_INVALID_INPUT_VALUE"));

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

        willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(updateSessionalPlanUseCase)
                .update(request.to(sessionalPlanId, userId));

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLAN_404_SESSIONAL_PLAN_NOT_FOUND"));

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

        willThrow(new ConflictException(SESSION_NAME_ALREADY_EXISTS))
                .given(updateSessionalPlanUseCase)
                .update(request.to(sessionalPlanId, userId));

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PLAN_409_SESSION_NAME_ALREADY_EXISTS"));

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

        willThrow(new BadRequestException(SESSION_PERIOD_CHANGE_NOT_ALLOWED))
                .given(updateSessionalPlanUseCase)
                .update(request.to(sessionalPlanId, userId));

        // when & then
        mockMvc.perform(put("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PLAN_400_SESSION_PERIOD_CHANGE_NOT_ALLOWED"));

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

        willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(deleteSessionalPlanUseCase)
                .delete(command);

        // when & then
        mockMvc.perform(delete("/sessional-plans/{sessionalPlanId}", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLAN_404_SESSIONAL_PLAN_NOT_FOUND"));

        then(deleteSessionalPlanUseCase).should().delete(command);
    }
}
