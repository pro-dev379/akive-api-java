package net.akive.api.plan.presentation;

import net.akive.api.plan.application.dto.result.SessionalPlanResult;
import net.akive.api.support.PrivateControllerTestSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SessionalPlanControllerTest extends PrivateControllerTestSupport {

    @Test
    void 세셔널_플랜_목록을_조회한다() throws Exception {
        // given
        Long userId = 1L;
        List<SessionalPlanResult> results = List.of(
                new SessionalPlanResult(
                        2L,
                        "2회차",
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 14)
                ),
                new SessionalPlanResult(
                        1L,
                        "1회차",
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 7)
                )
        );

        given(getSessionalPlansUseCase.getSessionalPlans(userId))
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

        then(getSessionalPlansUseCase).should().getSessionalPlans(userId);
    }

    @Test
    void 세셔널_플랜이_없으면_빈_목록을_응답한다() throws Exception {
        // given
        Long userId = 1L;

        given(getSessionalPlansUseCase.getSessionalPlans(userId))
                .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/sessional-plans")
                        .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionalPlans").isArray())
                .andExpect(jsonPath("$.sessionalPlans").isEmpty());

        then(getSessionalPlansUseCase).should().getSessionalPlans(userId);
    }
}
