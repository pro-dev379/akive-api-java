package net.akive.api.plan.presentation;

import net.akive.api.plan.application.dto.command.CreateTotalQuantityCommand;
import net.akive.api.plan.exception.PlanException;
import net.akive.api.plan.presentation.dto.request.CreateTotalQuantityRequest;
import net.akive.api.plan.presentation.dto.request.common.SubjectRequest;
import net.akive.api.plan.presentation.dto.request.common.TotalQuantityAmountRequest;
import net.akive.api.support.PrivateControllerTestSupport;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.CommonErrorCode.INVALID_INPUT_VALUE;
import static net.akive.api.plan.exception.PlanErrorCode.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.plan.exception.PlanErrorCode.SUBJECT_NAME_ALREADY_EXISTS;
import static net.akive.api.plan.exception.PlanErrorCode.TOTAL_QUANTITY_LIMIT_EXCEEDED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TotalQuantityControllerTest extends PrivateControllerTestSupport {

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
    void 세션_총량_생성_요청_시_과목명이_100자를_초과하면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("가".repeat(101), "수능"),
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
    void 세션_총량_생성_요청_시_과목_카테고리가_10자를_초과하면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "가".repeat(11)),
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
    void 세션_총량_생성_요청_시_메모가_25자를_초과하면_400을_응답한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "수능"),
                new TotalQuantityAmountRequest(100, "문제", "가".repeat(26))
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

        willThrow(new PlanException(TOTAL_QUANTITY_LIMIT_EXCEEDED))
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

        willThrow(new PlanException(SESSIONAL_PLAN_NOT_FOUND))
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

        willThrow(new PlanException(SUBJECT_NAME_ALREADY_EXISTS))
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

    private CreateTotalQuantityRequest createRequest() {
        return new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "수능"),
                new TotalQuantityAmountRequest(100, "문제", "총량 메모")
        );
    }
}
