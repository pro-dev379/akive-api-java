package net.akive.api.plan.presentation;

import net.akive.api.plan.application.dto.command.CreateTotalQuantityCommand;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.dto.command.DeleteTotalQuantityCommand;
import net.akive.api.plan.application.dto.query.GetSessionalPlanTotalQuantityQuery;
import net.akive.api.plan.application.dto.result.common.SessionWeekResult;
import net.akive.api.plan.application.dto.result.common.SubjectResult;
import net.akive.api.plan.application.dto.result.common.TotalQuantityAmountResult;
import net.akive.api.plan.application.dto.result.common.WeekPeriodResult;
import net.akive.api.plan.application.dto.result.sessionalplan.GetSessionalPlanResult;
import net.akive.api.plan.presentation.dto.request.CreateTotalQuantityRequest;
import net.akive.api.plan.presentation.dto.request.common.SubjectRequest;
import net.akive.api.plan.presentation.dto.request.common.TotalQuantityAmountRequest;
import net.akive.api.support.PrivateControllerTestSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.BadRequestError.INVALID_INPUT_VALUE;
import static net.akive.api.common.exception.business.BadRequestError.TOTAL_QUANTITY_LIMIT_EXCEEDED;
import static net.akive.api.common.exception.business.ConflictError.SUBJECT_NAME_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_TOTAL_QUANTITY_NOT_FOUND;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TotalQuantityControllerTest extends PrivateControllerTestSupport {

    @Test
    void 세션_총량을_조회한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        Long totalQuantityId = 10L;
        GetSessionalPlanTotalQuantityQuery query = new GetSessionalPlanTotalQuantityQuery(
                sessionalPlanId,
                userId,
                totalQuantityId
        );
        GetSessionalPlanResult.TotalQuantityResult result = new GetSessionalPlanResult.TotalQuantityResult(
                totalQuantityId,
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
        );

        given(getSessionalPlanTotalQuantityUseCase.get(query))
                .willReturn(result);

        // when & then
        mockMvc.perform(get("/sessional-plans/{sessionalPlanId}/total-quantities/{totalQuantityId}",
                        sessionalPlanId,
                        totalQuantityId
                )
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantityId").value(totalQuantityId))
                .andExpect(jsonPath("$.subject.name").value("수학"))
                .andExpect(jsonPath("$.subject.category").value("학습"))
                .andExpect(jsonPath("$.totalQuantityAmount.quantityAmount").value(100))
                .andExpect(jsonPath("$.totalQuantityAmount.unit").value("문제"))
                .andExpect(jsonPath("$.totalQuantityAmount.quantityMemo").value("총량 메모"))
                .andExpect(jsonPath("$.weeklyQuantities[0].weeklyQuantityId").value(100L))
                .andExpect(jsonPath("$.weeklyQuantities[0].sessionWeek.weekNum").value(1))
                .andExpect(jsonPath("$.weeklyQuantities[0].sessionWeek.weekPeriod.startOn").value("2026-07-06"))
                .andExpect(jsonPath("$.weeklyQuantities[0].sessionWeek.weekPeriod.endOn").value("2026-07-12"))
                .andExpect(jsonPath("$.weeklyQuantities[0].weeklyQuantityAmount.quantityAmount").value(70))
                .andExpect(jsonPath("$.weeklyQuantities[0].weeklyQuantityAmount.completedQuantityAmount").value(30))
                .andExpect(jsonPath("$.weeklyQuantities[0].weeklyQuantityAmount.delayedQuantityAmount").value(40))
                .andExpect(jsonPath("$.weeklyQuantities[0].weeklyQuantityAmount.quantityMemo").value("1주차 메모"));

        then(getSessionalPlanTotalQuantityUseCase).should().get(query);
    }

    @Test
    void 세션_총량_조회_요청_시_세셔널_플랜_식별자_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long totalQuantityId = 10L;

        // when & then
        mockMvc.perform(get("/sessional-plans/{sessionalPlanId}/total-quantities/{totalQuantityId}",
                        "invalid",
                        totalQuantityId
                )
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(getSessionalPlanTotalQuantityUseCase).should(never())
                .get(any(GetSessionalPlanTotalQuantityQuery.class));
    }

    @Test
    void 세션_총량_조회_요청_시_세션_총량_식별자_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;

        // when & then
        mockMvc.perform(get("/sessional-plans/{sessionalPlanId}/total-quantities/{totalQuantityId}",
                        sessionalPlanId,
                        "invalid"
                )
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(getSessionalPlanTotalQuantityUseCase).should(never())
                .get(any(GetSessionalPlanTotalQuantityQuery.class));
    }

    @Test
    void 세션_총량_조회_요청_시_세셔널_플랜이_없으면_404를_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        Long totalQuantityId = 10L;
        GetSessionalPlanTotalQuantityQuery query = new GetSessionalPlanTotalQuantityQuery(
                sessionalPlanId,
                userId,
                totalQuantityId
        );

        willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(getSessionalPlanTotalQuantityUseCase).get(query);

        // when & then
        mockMvc.perform(get("/sessional-plans/{sessionalPlanId}/total-quantities/{totalQuantityId}",
                        sessionalPlanId,
                        totalQuantityId
                )
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(SESSIONAL_PLAN_NOT_FOUND.getCode()));

        then(getSessionalPlanTotalQuantityUseCase).should().get(query);
    }

    @Test
    void 세션_총량_조회_요청_시_세션_총량이_없으면_404를_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        Long totalQuantityId = 10L;
        GetSessionalPlanTotalQuantityQuery query = new GetSessionalPlanTotalQuantityQuery(
                sessionalPlanId,
                userId,
                totalQuantityId
        );

        willThrow(new NotFoundException(SESSIONAL_PLAN_TOTAL_QUANTITY_NOT_FOUND))
                .given(getSessionalPlanTotalQuantityUseCase).get(query);

        // when & then
        mockMvc.perform(get("/sessional-plans/{sessionalPlanId}/total-quantities/{totalQuantityId}",
                        sessionalPlanId,
                        totalQuantityId
                )
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(SESSIONAL_PLAN_TOTAL_QUANTITY_NOT_FOUND.getCode()));

        then(getSessionalPlanTotalQuantityUseCase).should().get(query);
    }

    @Test
    void 세션_총량을_생성한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        Long totalQuantityId = 10L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "수능"),
                new TotalQuantityAmountRequest(100, "문제", "총량 메모")
        );
        CreateTotalQuantityCommand command = request.to(userId, sessionalPlanId);

        given(createTotalQuantityUseCase.create(command))
                .willReturn(totalQuantityId);

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalQuantityId").value(totalQuantityId));

        then(createTotalQuantityUseCase).should().create(command);
    }

    @Test
    void 세션_총량_생성_요청_시_세셔널_플랜_식별자_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        CreateTotalQuantityRequest request = createRequest();

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", "invalid")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(createTotalQuantityUseCase).should(never()).create(any(CreateTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_생성_요청_시_과목이_없으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                null,
                new TotalQuantityAmountRequest(100, "문제", "총량 메모")
        );

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(createTotalQuantityUseCase).should(never()).create(any(CreateTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_생성_요청_시_과목명이_50자를_초과하면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("가".repeat(51), "수능"),
                new TotalQuantityAmountRequest(100, "문제", "총량 메모")
        );

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(createTotalQuantityUseCase).should(never()).create(any(CreateTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_생성_요청_시_과목명이_비어있으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("", "수능"),
                new TotalQuantityAmountRequest(100, "문제", "총량 메모")
        );

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(createTotalQuantityUseCase).should(never()).create(any(CreateTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_생성_요청_시_과목_카테고리가_비어있으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", ""),
                new TotalQuantityAmountRequest(100, "문제", "총량 메모")
        );

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(createTotalQuantityUseCase).should(never()).create(any(CreateTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_생성_요청_시_과목_카테고리가_20자를_초과하면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "가".repeat(21)),
                new TotalQuantityAmountRequest(100, "문제", "총량 메모")
        );

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(createTotalQuantityUseCase).should(never()).create(any(CreateTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_생성_요청_시_총량이_없으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "수능"),
                null
        );

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(createTotalQuantityUseCase).should(never()).create(any(CreateTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_생성_요청_시_계획량이_없으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "수능"),
                new TotalQuantityAmountRequest(null, "문제", "총량 메모")
        );

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(createTotalQuantityUseCase).should(never()).create(any(CreateTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_생성_요청_시_계획량이_음수면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "수능"),
                new TotalQuantityAmountRequest(-1, "문제", "총량 메모")
        );

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(createTotalQuantityUseCase).should(never()).create(any(CreateTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_생성_요청_시_단위가_비어있으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "수능"),
                new TotalQuantityAmountRequest(100, "", "총량 메모")
        );

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(createTotalQuantityUseCase).should(never()).create(any(CreateTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_생성_요청_시_단위가_10자를_초과하면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "수능"),
                new TotalQuantityAmountRequest(100, "가".repeat(11), "총량 메모")
        );

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(createTotalQuantityUseCase).should(never()).create(any(CreateTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_생성_요청_시_메모가_비어있어도_세션_총량은_생성된다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        Long totalQuantityId = 10L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "수능"),
                new TotalQuantityAmountRequest(100, "문제", "")
        );
        CreateTotalQuantityCommand command = request.to(userId, sessionalPlanId);

        given(createTotalQuantityUseCase.create(command))
                .willReturn(totalQuantityId);

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalQuantityId").value(totalQuantityId));

        then(createTotalQuantityUseCase).should().create(command);
    }

    @Test
    void 세션_총량_생성_요청_시_메모가_null이어도_세션_총량을_생성한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        Long totalQuantityId = 10L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "수능"),
                new TotalQuantityAmountRequest(100, "문제", null)
        );
        CreateTotalQuantityCommand command = request.to(userId, sessionalPlanId);

        given(createTotalQuantityUseCase.create(command))
                .willReturn(totalQuantityId);

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalQuantityId").value(totalQuantityId));

        then(createTotalQuantityUseCase).should().create(command);
    }

    @Test
    void 세션_총량_생성_요청_시_메모가_100자를_초과하면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "수능"),
                new TotalQuantityAmountRequest(100, "문제", "가".repeat(101))
        );

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(createTotalQuantityUseCase).should(never()).create(any(CreateTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_개수가_제한을_초과하면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = createRequest();
        CreateTotalQuantityCommand command = request.to(userId, sessionalPlanId);

        willThrow(new BadRequestException(TOTAL_QUANTITY_LIMIT_EXCEEDED))
                .given(createTotalQuantityUseCase).create(command);

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(TOTAL_QUANTITY_LIMIT_EXCEEDED.getCode()));

        then(createTotalQuantityUseCase).should().create(command);
    }

    @Test
    void 세션_총량_생성_요청_시_요청_바디_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        String request = """
                {
                  "subject": {
                    "name": "수학",
                    "category": "수능"
                  },
                  "totalQuantityAmount": {
                    "quantityAmount": "not-integer",
                    "unit": "문제",
                    "quantityMemo": "총량 메모"
                  }
                }
                """;

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(createTotalQuantityUseCase).should(never()).create(any(CreateTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_생성_요청_시_세셔널_플랜이_없으면_404를_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "수능"),
                new TotalQuantityAmountRequest(100, "문제", "총량 메모")
        );
        CreateTotalQuantityCommand command = request.to(userId, sessionalPlanId);

        willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(createTotalQuantityUseCase).create(command);

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(SESSIONAL_PLAN_NOT_FOUND.getCode()));

        then(createTotalQuantityUseCase).should().create(command);
    }

    @Test
    void 세션_총량_생성_요청_시_과목명이_중복되면_409를_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = createRequest();
        CreateTotalQuantityCommand command = request.to(userId, sessionalPlanId);

        willThrow(new ConflictException(SUBJECT_NAME_ALREADY_EXISTS))
                .given(createTotalQuantityUseCase).create(command);

        // when & then
        mockMvc.perform(post("/sessional-plans/{sessionalPlanId}/total-quantities", sessionalPlanId)
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(SUBJECT_NAME_ALREADY_EXISTS.getCode()));

        then(createTotalQuantityUseCase).should().create(command);
    }

    @Test
    void 세션_총량을_삭제한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        Long totalQuantityId = 10L;
        DeleteTotalQuantityCommand command = new DeleteTotalQuantityCommand(
                sessionalPlanId,
                userId,
                totalQuantityId
        );

        // when & then
        mockMvc.perform(delete("/sessional-plans/{sessionalPlanId}/total-quantities/{totalQuantityId}",
                        sessionalPlanId,
                        totalQuantityId
                )
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isNoContent());

        then(deleteTotalQuantityUseCase).should().delete(command);
    }

    @Test
    void 세션_총량_삭제_요청_시_세셔널_플랜_식별자_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long totalQuantityId = 10L;

        // when & then
        mockMvc.perform(delete("/sessional-plans/{sessionalPlanId}/total-quantities/{totalQuantityId}",
                        "invalid",
                        totalQuantityId
                )
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(deleteTotalQuantityUseCase).should(never()).delete(any(DeleteTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_삭제_요청_시_세션_총량_식별자_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;

        // when & then
        mockMvc.perform(delete("/sessional-plans/{sessionalPlanId}/total-quantities/{totalQuantityId}",
                        sessionalPlanId,
                        "invalid"
                )
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()));

        then(deleteTotalQuantityUseCase).should(never()).delete(any(DeleteTotalQuantityCommand.class));
    }

    @Test
    void 세션_총량_삭제_요청_시_세션_총량이_없으면_404를_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        Long totalQuantityId = 10L;
        DeleteTotalQuantityCommand command = new DeleteTotalQuantityCommand(
                sessionalPlanId,
                userId,
                totalQuantityId
        );

        willThrow(new NotFoundException(SESSIONAL_PLAN_TOTAL_QUANTITY_NOT_FOUND))
                .given(deleteTotalQuantityUseCase).delete(command);

        // when & then
        mockMvc.perform(delete("/sessional-plans/{sessionalPlanId}/total-quantities/{totalQuantityId}",
                        sessionalPlanId,
                        totalQuantityId
                )
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(SESSIONAL_PLAN_TOTAL_QUANTITY_NOT_FOUND.getCode()));

        then(deleteTotalQuantityUseCase).should().delete(command);
    }

    private CreateTotalQuantityRequest createRequest() {
        return new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "수능"),
                new TotalQuantityAmountRequest(100, "문제", "총량 메모")
        );
    }
}
