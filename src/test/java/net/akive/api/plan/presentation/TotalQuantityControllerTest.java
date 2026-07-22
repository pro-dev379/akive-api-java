package net.akive.api.plan.presentation;

import net.akive.api.plan.application.dto.command.CreateTotalQuantityCommand;
import net.akive.api.plan.exception.PlanException;
import net.akive.api.plan.presentation.dto.request.CreateTotalQuantityRequest;
import net.akive.api.plan.presentation.dto.request.common.SubjectRequest;
import net.akive.api.plan.presentation.dto.request.common.TotalQuantityAmountRequest;
import net.akive.api.support.PrivateControllerTestSupport;
import org.junit.jupiter.api.Test;

import static net.akive.api.plan.exception.PlanErrorCode.SESSIONAL_PLAN_NOT_FOUND;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
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
                new SubjectRequest("수학", "과목"),
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
    void 세셔널_플랜이_없으면_세션_총량_생성에_실패한다() throws Exception {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 2L;
        CreateTotalQuantityRequest request = new CreateTotalQuantityRequest(
                new SubjectRequest("수학", "과목"),
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
}