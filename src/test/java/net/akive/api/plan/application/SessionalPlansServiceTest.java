package net.akive.api.plan.application;

import net.akive.api.plan.application.dto.result.SessionalPlanResult;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
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
class SessionalPlansServiceTest {

    @Mock
    LoadSessionalPlanPort loadSessionalPlanPort;

    @InjectMocks
    SessionalPlansService sessionalPlansService;

    @Test
    void 회원의_세셔널_플랜_목록을_조회한다() {
        // given
        Long userId = 1L;
        SessionalPlan firstPlan = SessionalPlan.create(
                2L,
                userId,
                "2회차",
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 14)
        );
        SessionalPlan secondPlan = SessionalPlan.create(
                1L,
                userId,
                "1회차",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 7)
        );

        given(loadSessionalPlanPort.loadAllByUserIdOrderBySessionalPlanIdDesc(userId))
                .willReturn(List.of(firstPlan, secondPlan));

        // when
        List<SessionalPlanResult> results = sessionalPlansService.getSessionalPlans(userId);

        // then
        assertThat(results)
                .extracting(
                        SessionalPlanResult::sessionalPlanId,
                        SessionalPlanResult::sessionName,
                        SessionalPlanResult::sessionStartOn,
                        SessionalPlanResult::sessionEndOn
                )
                .containsExactly(
                        tuple(
                                2L,
                                "2회차",
                                LocalDate.of(2026, 7, 8),
                                LocalDate.of(2026, 7, 14)
                        ),
                        tuple(
                                1L,
                                "1회차",
                                LocalDate.of(2026, 7, 1),
                                LocalDate.of(2026, 7, 7)
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
        List<SessionalPlanResult> results = sessionalPlansService.getSessionalPlans(userId);

        // then
        assertThat(results).isEmpty();

        then(loadSessionalPlanPort).should().loadAllByUserIdOrderBySessionalPlanIdDesc(userId);
    }
}
